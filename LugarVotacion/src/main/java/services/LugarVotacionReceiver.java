package services;

import lugarVotacion.Mesa;
import lugarVotacion.Voto;
import lugarVotacion.ValidacionCedula;
import lugarVotacion.Candidato;
import com.zeroc.Ice.Current;

import java.util.Arrays;

import com.zeroc.Ice.Communicator;
import model.Message;
import reliableMessage.RMSourcePrx;
import reliableMessage.RMDestinationPrx;
import broker.BrokerServicePrx;
import threads.RMJob;
import communication.Notification;

public class LugarVotacionReceiver implements Mesa {

    private RMSourcePrx rm;
    private final BrokerServicePrx broker;
    private final String idLugar;
    private final Communicator communicator;
    private final RMJob job; // NUEVO: Para manejar reintentos locales
    private final Notification notification; // NUEVO: Para configurar destino
    private int contadorVotos = 0;
    private static final int REBALANCE_INTERVAL = 20;

    public LugarVotacionReceiver(BrokerServicePrx broker, String idLugar, Communicator communicator, 
                                RMJob job, Notification notification) {
        this.broker = broker;
        this.idLugar = idLugar;
        this.communicator = communicator;
        this.job = job;
        this.notification = notification;
        
        // Obtener proxy inicial
        this.rm = obtenerNuevoProxy();
        
        // NUEVO: Configurar el destino para el RMJob
        if (this.rm != null) {
            // Convertir RMSourcePrx a RMDestinationPrx para el notification
            // Esto es necesario porque RMJob espera enviar a un RMDestination
            String proxyString = communicator.proxyToString(this.rm);
            try {
                RMDestinationPrx destination = RMDestinationPrx.checkedCast(
                    communicator.stringToProxy(proxyString));
                if (destination != null) {
                    this.notification.setService(destination);
                    System.out.println("Destino configurado para RMJob local");
                }
            } catch (Exception e) {
                System.err.println("Error configurando destino para RMJob: " + e.getMessage());
            }
        }
    }

    @Override
    public ValidacionCedula consultarCedula(String cedula, int mesaId, Current current) {
        System.out.println("Consulta de cédula recibida: " + cedula + " para mesa: " + mesaId);
        
        ValidacionCedula resultado = new ValidacionCedula();
        
        try {
            if (rm != null) {
                // Llamar al proxy para validar la cédula
                String respuesta = rm.consultarValidezCiudadano(cedula, mesaId);
                
                // Procesar la respuesta del servidor
                procesarRespuestaValidacion(respuesta, resultado);
                
                System.out.println("Respuesta de validación: " + resultado.mensaje);
            } else {
                resultado.esValida = false;
                resultado.mensaje = "Error: No hay conexión con el servidor";
                System.err.println("No hay proxy disponible para consultar cédula");
            }
        } catch (Exception e) {
            System.err.println("Error consultando cédula: " + e.getMessage());
            resultado.esValida = false;
            resultado.mensaje = "Error de conexión: " + e.getMessage();
            
            // Intentar obtener un nuevo proxy en caso de error
            System.out.println("Intentando obtener nuevo proxy debido a error...");
            RMSourcePrx nuevoProxy = obtenerNuevoProxy();
            if (nuevoProxy != null) {
                this.rm = nuevoProxy;
                try {
                    String respuesta = rm.consultarValidezCiudadano(cedula, mesaId);
                    procesarRespuestaValidacion(respuesta, resultado);
                } catch (Exception e2) {
                    System.err.println("Error crítico consultando cédula: " + e2.getMessage());
                    resultado.esValida = false;
                    resultado.mensaje = "Error crítico de conexión";
                }
            }
        }
        
        return resultado;
    }

    private void procesarRespuestaValidacion(String respuesta, ValidacionCedula resultado) {
        // Formato esperado: "VALIDA:mensaje" o "INVALIDA:mensaje" o "ERROR:mensaje"
        String[] partes = respuesta.split(":", 2);
        if (partes.length == 2) {
            String estado = partes[0];
            String mensaje = partes[1];
            
            switch (estado) {
                case "VALIDA":
                    resultado.esValida = true;
                    resultado.mensaje = mensaje;
                    break;
                case "INVALIDA":
                    resultado.esValida = false;
                    resultado.mensaje = mensaje;
                    break;
                case "ERROR":
                default:
                    resultado.esValida = false;
                    resultado.mensaje = mensaje;
                    break;
            }
        } else {
            resultado.esValida = false;
            resultado.mensaje = "Error procesando respuesta del servidor";
        }
    }

    @Override
    public void enviarVoto(Voto voto, Current current) {
        System.out.println("Voto recibido: id " + voto.idVoto);
        
        // Incrementar contador y verificar si necesita rebalanceo
        contadorVotos++;
        if (contadorVotos % REBALANCE_INTERVAL == 0) {
            System.out.println("Realizando rebalanceo de proxy (voto #" + contadorVotos + ")");
            RMSourcePrx nuevoProxy = obtenerNuevoProxy();
            if (nuevoProxy != null) {
                this.rm = nuevoProxy;
                
                // NUEVO: Actualizar destino en notification
                String proxyString = communicator.proxyToString(this.rm);
                try {
                    RMDestinationPrx destination = RMDestinationPrx.checkedCast(
                        communicator.stringToProxy(proxyString));
                    if (destination != null) {
                        this.notification.setService(destination);
                        System.out.println("Destino actualizado en RMJob tras rebalanceo");
                    }
                } catch (Exception e) {
                    System.err.println("Error actualizando destino tras rebalanceo: " + e.getMessage());
                }
                
                System.out.println("Proxy actualizado exitosamente");
            } else {
                System.err.println("No se pudo obtener nuevo proxy, manteniendo el actual");
            }
        }

        // NUEVO: Convertir Voto a Message y usar RMJob para envío confiable
        Message msg = new Message();
        msg.idVoto = voto.idVoto;
        msg.message = voto.idCandidato + "|" + voto.fecha;

        // Agregar mensaje al RMJob para envío confiable local
        job.add(msg);
        System.out.println("Voto #" + contadorVotos + " agregado a cola de reintentos local.");
    }
    
    private RMSourcePrx obtenerNuevoProxy() {
        try {
            String proxyString = broker.obtenerProxy(idLugar);
            if (proxyString == null) {
                System.err.println("No hay proxies disponibles en el Broker.");
                return null;
            }

            System.out.println("Nuevo proxy obtenido del broker: " + proxyString);
            RMSourcePrx nuevoRm = RMSourcePrx.checkedCast(communicator.stringToProxy(proxyString));
            
            if (nuevoRm == null) {
                System.err.println("No se pudo obtener el proxy RMSource del ProxySync.");
                return null;
            }
            
            return nuevoRm;
        } catch (Exception e) {
            System.err.println("Error obteniendo nuevo proxy: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Candidato[] obtenerCandidatos(Current current) {
        System.out.println("Solicitud de lista de candidatos recibida");
        
        try {
            if (rm == null) {
                throw new RuntimeException("No hay conexión con el servidor");
            }
            
            // Obtener la lista de candidatos como string formateado
            String candidatosStr = rm.listarCandidatos();
            
            if (candidatosStr == null || candidatosStr.startsWith("ERROR:")) {
                throw new RuntimeException(candidatosStr != null ? candidatosStr : "Respuesta vacía del servidor");
            }
            
            // Parsear el string en formato "id|nombre|partido;id|nombre|partido;..."
            return Arrays.stream(candidatosStr.split(";"))
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        String[] partes = s.split("\\|");
                        if (partes.length != 3) {
                            throw new RuntimeException("Formato de candidato inválido: " + s);
                        }
                        Candidato c = new Candidato();
                        c.id = Integer.parseInt(partes[0]);
                        c.nombre = partes[1];
                        c.partido = partes[2];
                        return c;
                    })
                    .toArray(Candidato[]::new);
                    
        } catch (Exception e) {
            System.err.println("Error obteniendo candidatos: " + e.getMessage());
            
            // Intentar obtener un nuevo proxy en caso de error
            System.out.println("Intentando obtener nuevo proxy debido a error...");
            RMSourcePrx nuevoProxy = obtenerNuevoProxy();
            if (nuevoProxy != null) {
                this.rm = nuevoProxy;
                try {
                    // Reintentar con el nuevo proxy
                    return obtenerCandidatos(current);
                } catch (Exception e2) {
                    throw new RuntimeException("Error crítico obteniendo candidatos: " + e2.getMessage());
                }
            }
            
            throw new RuntimeException("Error obteniendo lista de candidatos: " + e.getMessage());
        }
    }
}