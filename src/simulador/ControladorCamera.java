package simulador;

import br.com.davidbuzatto.jsge.core.Camera2D;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;

/**
 * Câmera 2D reutilizável por qualquer janela de árvore.
 * Zoom com a roda do mouse, centrado no cursor, e arraste em área vazia.
 */
public class ControladorCamera {

    private static final double ZOOM_MIN = 0.3;
    private static final double ZOOM_MAX = 3.0;
    private static final double FATOR_ZOOM = 1.1;

    private final Camera2D camera;
    private Vector2 ultimoMouse;
    private boolean arrastando;

    public ControladorCamera() {
        camera = new Camera2D();
        camera.target.x = 0;
        camera.target.y = 0;
        camera.offset.x = 0;
        camera.offset.y = 0;
        camera.zoom = 1;
        ultimoMouse = new Vector2();
    }

    /** Câmera da engine, para usar em beginMode2D / endMode2D. */
    public Camera2D camera() {
        return camera;
    }

    /**
     * Atualiza zoom e arraste. Passe bloqueado = true quando o mouse estiver
     * sobre a toolbar ou sobre um nó.
     */
    public void atualizar( EngineFrame e, Vector2 mouse, boolean bloqueado ) {
        double roda = e.getMouseWheelMove();
        if ( roda != 0 && mouse != null && !bloqueado ) {
            zoomEm( mouse, roda > 0 ? FATOR_ZOOM : 1 / FATOR_ZOOM );
        }

        if ( mouse != null && !bloqueado && e.isMouseButtonPressed( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            arrastando = true;
        }
        if ( !e.isMouseButtonDown( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            arrastando = false;
        }
        if ( arrastando && mouse != null && ultimoMouse != null ) {
            camera.target.x -= ( mouse.x - ultimoMouse.x ) / camera.zoom;
            camera.target.y -= ( mouse.y - ultimoMouse.y ) / camera.zoom;
        }
        ultimoMouse = mouse;
    }

    /** Converte uma posição da tela para o mundo. */
    public Vector2 telaParaMundo( Vector2 m ) {
        return new Vector2(
                ( m.x - camera.offset.x ) / camera.zoom + camera.target.x,
                ( m.y - camera.offset.y ) / camera.zoom + camera.target.y
        );
    }

    private void zoomEm( Vector2 mouse, double fator ) {
        Vector2 antes = telaParaMundo( mouse );
        camera.zoom = Math.max( ZOOM_MIN, Math.min( ZOOM_MAX, camera.zoom * fator ) );
        camera.target.x = antes.x - ( mouse.x - camera.offset.x ) / camera.zoom;
        camera.target.y = antes.y - ( mouse.y - camera.offset.y ) / camera.zoom;
    }
}