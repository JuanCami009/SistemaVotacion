package services;

import lugarVotacion.Mesa;
import lugarVotacion.Voto;
import lugarVotacion.ValidacionCedula;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.Communicator;
import model.Message;
import reliableMessage.RMSourcePrx;
import broker.BrokerServicePrx;

public class LugarVotacionReceiver implements Mesa {

    private RMSourcePrx rm;
    private final BrokerServicePrx broker;
    private final String idLugar;
    private final Communicator communicator;
    private int contadorVotos = 0;
    private static final int REBALANCE_INTERVAL = 20;

    public LugarVotacionReceiver(BrokerServicePrx broker, String idLugar, Communicator communicator) {
        this.broker = broker;
        this.idLugar = idLugar;
        this.communicator = communicator;
        
        // Obtener proxy inicial
        this.rm = obtenerNuevoProxy();
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
                System.out.println("Proxy actualizado exitosamente");
            } else {
                System.err.println("No se pudo obtener nuevo proxy, manteniendo el actual");
            }
        }

        // Convertir Voto a Message incluyendo el idVoto explícitamente
        Message msg = new Message();
        msg.idVoto = voto.idVoto;
        msg.message = "Voto id: " + voto.idVoto + " desde " + idLugar;

        try {
            if (rm != null) {
                rm.sendMessage(msg);
                System.out.println("Voto #" + contadorVotos + " reenviado a reliable message.");
            } else {
                System.err.println("No hay proxy disponible para enviar el voto");
            }
        } catch (Exception e) {
            System.err.println("Error reenviando voto: " + e.getMessage());
            
            // En caso de error, intentar obtener un nuevo proxy
            System.out.println("Intentando obtener nuevo proxy debido a error...");
            RMSourcePrx nuevoProxy = obtenerNuevoProxy();
            if (nuevoProxy != null) {
                this.rm = nuevoProxy;
                try {
                    rm.sendMessage(msg);
                    System.out.println("Voto reenviado exitosamente con nuevo proxy.");
                } catch (Exception e2) {
                    System.err.println("Error crítico: No se pudo reenviar el voto incluso con nuevo proxy: " + e2.getMessage());
                }
            }
        }
    }
    
    /**
     * Obtiene un nuevo proxy del broker
     */
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
}