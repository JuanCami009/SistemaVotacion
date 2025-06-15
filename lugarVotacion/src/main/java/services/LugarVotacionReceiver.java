package services;

import lugarVotacion.Mesa;
import lugarVotacion.Voto;
import lugarVotacion.ValidacionCedula;
import lugarVotacion.Candidato;
import com.zeroc.Ice.Current;
import threads.LocalRetryJob;

import java.util.Arrays;

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
    private final LocalRetryJob retryJob;

    public LugarVotacionReceiver(BrokerServicePrx broker, String idLugar, Communicator communicator) {
        this.broker = broker;
        this.idLugar = idLugar;
        this.communicator = communicator;

        this.rm = obtenerNuevoProxy();

        this.retryJob = new LocalRetryJob(broker, idLugar);  // ← corregido
        this.retryJob.start();
        if (this.rm != null) {
            this.retryJob.actualizarProxy(this.rm);
        }
    }

    @Override
    public ValidacionCedula consultarCedula(String cedula, int mesaId, Current current) {
        System.out.println("Consulta de cédula recibida: " + cedula + " para mesa: " + mesaId);
        ValidacionCedula resultado = new ValidacionCedula();

        try {
            if (rm != null) {
                String respuesta = rm.consultarValidezCiudadano(cedula, mesaId);
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

            System.out.println("Intentando obtener nuevo proxy debido a error...");
            RMSourcePrx nuevoProxy = obtenerNuevoProxy();
            if (nuevoProxy != null) {
                this.rm = nuevoProxy;
                this.retryJob.actualizarProxy(nuevoProxy);
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
        contadorVotos++;

        if (contadorVotos % REBALANCE_INTERVAL == 0) {
            System.out.println("Realizando rebalanceo de proxy (voto #" + contadorVotos + ")");
            RMSourcePrx nuevoProxy = obtenerNuevoProxy();
            if (nuevoProxy != null) {
                this.rm = nuevoProxy;
                this.retryJob.actualizarProxy(nuevoProxy);
                System.out.println("Proxy actualizado exitosamente");
            } else {
                System.err.println("No se pudo obtener nuevo proxy, manteniendo el actual");
            }
        }

        Message msg = new Message();
        msg.idVoto = voto.idVoto;
        msg.message = voto.idCandidato + "|" + voto.fecha;

        try {
            if (rm != null) {
                rm.sendMessage(msg);
                System.out.println("Voto #" + contadorVotos + " enviado exitosamente");
            } else {
                System.out.println("Proxy no disponible, se encola el voto para reintento.");
                retryJob.agregarMensaje(msg);
            }
        } catch (Exception e) {
            System.err.println("Error al enviar el voto, se encola para reintento: " + e.getMessage());
            retryJob.agregarMensaje(msg);
        }
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
            if (rm == null) throw new RuntimeException("No hay conexión con el servidor");

            String candidatosStr = rm.listarCandidatos();
            if (candidatosStr == null || candidatosStr.startsWith("ERROR:")) {
                throw new RuntimeException(candidatosStr != null ? candidatosStr : "Respuesta vacía del servidor");
            }

            return Arrays.stream(candidatosStr.split(";"))
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        String[] partes = s.split("\\|");
                        if (partes.length != 3) throw new RuntimeException("Formato de candidato inválido: " + s);
                        Candidato c = new Candidato();
                        c.id = Integer.parseInt(partes[0]);
                        c.nombre = partes[1];
                        c.partido = partes[2];
                        return c;
                    })
                    .toArray(Candidato[]::new);

        } catch (Exception e) {
            System.err.println("Error obteniendo candidatos: " + e.getMessage());
            System.out.println("Intentando obtener nuevo proxy debido a error...");
            RMSourcePrx nuevoProxy = obtenerNuevoProxy();
            if (nuevoProxy != null) {
                this.rm = nuevoProxy;
                this.retryJob.actualizarProxy(nuevoProxy);
                try {
                    return obtenerCandidatos(current);
                } catch (Exception e2) {
                    throw new RuntimeException("Error crítico obteniendo candidatos: " + e2.getMessage());
                }
            }

            throw new RuntimeException("Error obteniendo lista de candidatos: " + e.getMessage());
        }
    }
}
