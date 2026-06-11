package evaluacion2.streamflow.exception;


public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no fue encontrado");
    }
}
