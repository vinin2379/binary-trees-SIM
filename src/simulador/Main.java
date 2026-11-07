package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import java.awt.Point;
import simulador.abb.JanelaSimuladorABB;
import simulador.ui.Tema;
import simulador.avl.JanelaSimuladorAVL;

/**
 * Janela do menu. Título e nomes entram letra a letra; ao escolher um simulador,
 * menu, nomes e título saem. A janela é levada para fora da tela (em vez de
 * escondida) para continuar desenhando e voltar depois.
 *
 * @author Vinícius Lima Sobon
 */
public class Main extends EngineFrame {

    private static final int LARG = 800;
    private static final int ALT = 600;

    private static final double RAIO_TRIGGER = 24;
    private static final double RAIO_ITEM = 23;
    private static final double RAIO_CIRCULO = 120;
    private static final double ATRASO_ABRIR = 0.1;
    private static final double ATRASO_FECHAR = 0.07;
    private static final double TEMPO_LIBERAR_CLIQUE = 0.3;
    private static final double DURACAO_TREMOR = 0.35;
    private static final int TAM_ICONE = 14;
    private static final int TAM_ROTULO = 14;

    private static final String TITULO = "Simulador de Árvores";
    private static final String NOMES = "Arthur Machado  -  Vinicius Sobon";
    private static final int TAM_TITULO = 52;
    private static final int TAM_NOMES = 22;
    private static final double Y_TITULO = 130;
    private static final double Y_NOMES = 540;
    private static final double CENTRO_Y = 340;

    private static final double DURACAO_LETRA = 0.5;
    private static final double ATRASO_POR_LETRA = 0.02;
    private static final double DESLOCAMENTO_Y = 30;

    // saída: menu e nomes juntos, depois o título
    private static final double DURACAO_SAIDA_MENU = 0.45;
    private static final double INICIO_SAIDA_NOMES = 0.0;
    private static final double INICIO_SAIDA_TITULO = 0.4;
    private static final double DURACAO_SAIDA_LETRA = 0.4;
    private static final double ATRASO_SAIDA_LETRA = 0.025;
    private static final double DESLOCAMENTO_SAIDA = 40;

    public static Main instancia;

    private static class Item {

        final String icone;
        final String rotulo;
        final int acao;
        final boolean ativo;

        Vector2 pos = new Vector2();
        Vector2 vel = new Vector2();
        Vector2 alvo = new Vector2();

        double atraso;
        boolean pendente;
        double escala = 1;
        double alpha;
        boolean hover;
        double tempo;
        double fase;
        double chacoalhar;
        boolean hoverAnt;

        Item( String icone, String rotulo, int acao, boolean ativo ) {
            this.icone = icone;
            this.rotulo = rotulo;
            this.acao = acao;
            this.ativo = ativo;
        }
    }

    private Item[] itens;
    private Vector2 centro;
    private boolean aberto;
    private boolean triggerHover;
    private double tremor;
    private double tempo;
    private double tempoAberto;

    private double tempoRevelacao;
    private boolean revelacaoTerminou;
    private double progressoTrigger;

    private boolean saindo;
    private double tempoSaida;
    private int acaoPendente = -1;

    private Point posMenu;

    public Main() {
        super( LARG, ALT, "Simulador de Árvores Binárias", 60, true );
        instancia = this;
    }

    @Override
    public void create() {
        centro = new Vector2( LARG / 2.0, CENTRO_Y );

        itens = new Item[] {
            new Item( "ABB", "Árvore Binária de Busca", 0, true ),
            new Item( "AVL", "Árvore AVL", 1, true ),
            new Item( "RN", "Rubro-Negra (em breve)", 2, false ),
            new Item( "Sair", "Sair", 3, true )
        };

        for ( int i = 0; i < itens.length; i++ ) {
            itens[i].fase = i * 1.7;
        }
        reiniciarEntrada();
    }

    @Override
    public void update( double delta ) {
        tempo += delta;
        if ( tremor > 0 ) {
            tremor -= delta;
        }

        if ( saindo ) {
            tempoSaida += delta;
            double fimSaida = Math.max( DURACAO_SAIDA_MENU,
                    INICIO_SAIDA_TITULO + TITULO.length() * ATRASO_SAIDA_LETRA + DURACAO_SAIDA_LETRA );
            if ( tempoSaida >= fimSaida ) {
                saindo = false;
                executarAcao( acaoPendente );
            }
            return;
        }

        if ( aberto ) {
            tempoAberto += delta;
        }

        if ( !revelacaoTerminou ) {
            tempoRevelacao += delta;
            if ( tempoRevelacao >= duracaoTotalRevelacao() ) {
                revelacaoTerminou = true;
            }
        }

        if ( revelacaoTerminou && progressoTrigger < 1 ) {
            progressoTrigger = Math.min( 1, progressoTrigger + delta * 1.8 );
        }

        boolean interativo = aberto && tempoAberto >= TEMPO_LIBERAR_CLIQUE;

        Vector2 mouse = getMousePositionPoint();
        triggerHover = mouse != null && mouse.distance( centro ) <= RAIO_TRIGGER;

        if ( revelacaoTerminou && mouse != null && isMouseButtonPressed( MOUSE_BUTTON_LEFT ) ) {
            if ( triggerHover ) {
                alternarMenu();
            } else if ( interativo ) {
                for ( Item it : itens ) {
                    if ( it.ativo && mouse.distance( it.pos ) <= RAIO_ITEM * it.escala ) {
                        iniciarSaida( it.acao );
                        break;
                    }
                }
            }
        }

        for ( int i = 0; i < itens.length; i++ ) {
            Item it = itens[i];

            if ( it.pendente ) {
                it.atraso -= delta;
                if ( it.atraso <= 0 ) {
                    it.pendente = false;
                    it.alvo = aberto ? pontoNoCirculo( i ) : new Vector2( centro.x, centro.y );
                }
            }

            it.vel.x += ( ( it.alvo.x - it.pos.x ) * 300 - it.vel.x * 28 ) * delta;
            it.vel.y += ( ( it.alvo.y - it.pos.y ) * 300 - it.vel.y * 28 ) * delta;
            it.pos.x += it.vel.x * delta;
            it.pos.y += it.vel.y * delta;

            it.hover = interativo && mouse != null && mouse.distance( it.pos ) <= RAIO_ITEM;

            it.tempo += delta;
            if ( it.chacoalhar > 0 ) {
                it.chacoalhar = Math.max( 0, it.chacoalhar - delta );
            }
            if ( it.hover && !it.hoverAnt ) {
                it.chacoalhar = 0.35;
            }
            it.hoverAnt = it.hover;

            double escalaAlvo = it.hover ? 1.1 : 1.0;
            it.escala += ( escalaAlvo - it.escala ) * Math.min( 1, delta * 15 );

            double alphaAlvo = aberto ? 1.0 : 0.0;
            it.alpha += ( alphaAlvo - it.alpha ) * Math.min( 1, delta * 10 );
        }
    }

    @Override
    
    public void draw() {
        clearBackground( Tema.FUNDO );
        

        if ( saindo ) {
            desenharSaida();
            return;
        } else {
            
        desenharEntrada();
        
        }

        
    }

    
    private void desenharEntrada(){
        
        desenharRevelado( TITULO, TAM_TITULO, Y_TITULO, 0 );
        desenharRevelado( NOMES, TAM_NOMES, Y_NOMES, TITULO.length() );

        double entrada = easeOutBack( progressoTrigger );
        double yTrigger = centro.y + ( 1 - entrada ) * 40;

        desenharItens( 0 );

        double dx = tremor > 0 ? Math.sin( tempo * 60 ) * 2 : 0;
        desenharTrigger( centro.x + dx, yTrigger, RAIO_TRIGGER * Math.max( 0, entrada ), entrada > 0.5 );
        
    }
    
    /**
     * Desenha os itens do menu. saida vai de 0 (menu normal) a 1 (menu totalmente
     * recolhido no centro e invisível).
     */
    
    private void desenharItens( double saida ) {
        double k = 1 - saida;
        for ( Item it : itens ) {
            double fator = ( it.ativo ? it.alpha : it.alpha * 0.5 ) * k;
            if ( fator <= 0.01 ) {
                continue;
            }

            double bx = lerp( it.pos.x, centro.x, saida );
            double by = lerp( it.pos.y, centro.y, saida );
            double raio = RAIO_ITEM * it.escala * k;
            boolean destacado = it.hover && saida == 0;

            Color corFundo = ColorUtils.fade( destacado ? Tema.ITEM_HOVER : Tema.ITEM, fator );
            Color corTexto = ColorUtils.fade( Tema.TEXTO, fator );

            Vector2 p = posVisual( it, bx, by );
            fillCircle( p, raio, corFundo );

            int larg = measureText( it.icone, TAM_ICONE );
            drawText( it.icone, p.x - larg / 2.0, p.y - TAM_ICONE / 3.0, TAM_ICONE, corTexto );

            if ( destacado && it.alpha > 0.5 ) {
                int largRotulo = measureText( it.rotulo, TAM_ROTULO );
                drawText( it.rotulo, p.x - largRotulo / 2.0, p.y + raio + 6, TAM_ROTULO, Tema.TEXTO );
            }
        }
    }

    private void desenharTrigger( double x, double y, double raio, boolean mostrarTexto ) {
        if ( raio <= 0.5 ) {
            return;
        }
        fillCircle( new Vector2( x, y ), raio, triggerHover ? Tema.TRIGGER_HOVER : Tema.TRIGGER );
        if ( mostrarTexto ) {
            String txt = aberto ? "X" : "MENU";
            int tam = 15;
            int larg = measureText( txt, tam );
            drawText( txt, x - ( larg / 2.0 ) + 1, y - 5, tam, Tema.TEXTO_CLARO );
        }
    }

    /**
     * Chamado pela janela do simulador ao clicar em "Voltar".
     * Traz a janela de volta e repete a animação de entrada.
     */
    public void mostrar() {
        if ( posMenu != null ) {
            setLocation( posMenu );
        }
        toFront();
        reiniciarEntrada();
    
    }

    private void reiniciarEntrada() {
        tempoRevelacao = 0;
        revelacaoTerminou = false;
        progressoTrigger = 0;
        aberto = false;
        tempoAberto = 0;
        saindo = false;
        tempoSaida = 0;
        for ( Item it : itens ) {
            it.pos = new Vector2( centro.x, centro.y );
            it.vel = new Vector2();
            it.alvo = new Vector2( centro.x, centro.y );
            it.pendente = false;
            it.alpha = 0;
            it.escala = 1;
            it.hover = false;
            it.hoverAnt = false;
        }
    }

    private void desenharRevelado( String texto, int tamanho, double y, int deslocamento ) {
        int larguraTotal = measureText( texto, tamanho );
        double x = LARG / 2.0 - larguraTotal / 2.0;
        for ( int i = 0; i < texto.length(); i++ ) {
            String s = String.valueOf( texto.charAt( i ) );
            double inicio = ( deslocamento + i ) * ATRASO_POR_LETRA;
            double progresso = ( tempoRevelacao - inicio ) / DURACAO_LETRA;
            if ( progresso > 0 ) {
                double f = easeInOut( Math.min( 1, progresso ) );
                drawText( s, x, y + ( 1 - f ) * DESLOCAMENTO_Y, tamanho, ColorUtils.fade( Tema.TEXTO, f ) );
            }
            x += measureText( s, tamanho );
        }
    }

    /** Saída: menu recolhe enquanto os nomes saem da esquerda para a direita; depois o título. */
    private void desenharSaida() {
        double eMenu = easeInOut( Math.min( 1, tempoSaida / DURACAO_SAIDA_MENU ) );
        desenharItens( eMenu );
        desenharTrigger( centro.x, centro.y, RAIO_TRIGGER * ( 1 - eMenu ), false );

        desenharLetrasSaindo( NOMES, TAM_NOMES, Y_NOMES, INICIO_SAIDA_NOMES );
        desenharLetrasSaindo( TITULO, TAM_TITULO, Y_TITULO, INICIO_SAIDA_TITULO );
    }

    private void desenharLetrasSaindo( String texto, int tamanho, double y, double inicioSaida ) {
        int larguraTotal = measureText( texto, tamanho );
        double x = LARG / 2.0 - larguraTotal / 2.0;
        for ( int i = 0; i < texto.length(); i++ ) {
            String s = String.valueOf( texto.charAt( i ) );
            double inicio = inicioSaida + i * ATRASO_SAIDA_LETRA;
            double progresso = ( tempoSaida - inicio ) / DURACAO_SAIDA_LETRA;
            if ( progresso < 1 ) {
                double f = easeInOut( Math.max( 0, progresso ) );
                double alfa = 1 - f;
                if ( alfa > 0.01 ) {
                    drawText( s, x, y - f * DESLOCAMENTO_SAIDA, tamanho, ColorUtils.fade( Tema.TEXTO, alfa ) );
                }
            }
            x += measureText( s, tamanho );
        }
    }

    private void iniciarSaida( int acao ) {
        saindo = true;
        tempoSaida = 0;
        acaoPendente = acao;
        for ( Item it : itens ) {
            it.hover = false;
        }
    }

    private void alternarMenu() {
        aberto = !aberto;
        tempoAberto = 0;
        for ( int i = 0; i < itens.length; i++ ) {
            Item it = itens[i];
            it.pendente = true;
            it.atraso = i * ( aberto ? ATRASO_ABRIR : ATRASO_FECHAR );
        }
        if ( !aberto ) {
            tremor = DURACAO_TREMOR;
        }
    }

    private void executarAcao( int acao ) {
        if ( acao == 0 || acao == 1 ) {
            posMenu = getLocation();
            setLocation( -3000, -3000 );
            if ( acao == 0 ) {
                new Thread( () -> new JanelaSimuladorABB() ).start();
            } else {
                new Thread( () -> new JanelaSimuladorAVL() ).start();
            }
        } else if ( acao == 3 ) {
            System.exit( 0 );
        }
    }

    private static double lerp( double a, double b, double t ) {
        return a + ( b - a ) * t;
    }

    private static double easeOutBack( double t ) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow( t - 1, 3 ) + c1 * Math.pow( t - 1, 2 );
    }

    private static double easeInOut( double t ) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow( -2 * t + 2, 2 ) / 2;
    }

    private Vector2 pontoNoCirculo( int i ) {
        double angulo = ( 2 * Math.PI * i ) / itens.length - ( Math.PI / 2 );
        return new Vector2(
                centro.x + RAIO_CIRCULO * Math.cos( angulo ),
                centro.y + RAIO_CIRCULO * Math.sin( angulo )
        );
    }

    private Vector2 posVisual( Item it, double bx, double by ) {
        double ox = Math.sin( it.tempo * 0.9 + it.fase ) * 1.5;
        double oy = Math.cos( it.tempo * 0.7 + it.fase * 1.3 ) * 2;
        if ( it.chacoalhar > 0 ) {
            ox += Math.sin( it.chacoalhar * 55 ) * ( it.chacoalhar / 0.35 * 3 );
        }
        return new Vector2( bx + ox, by + oy );
    }

    private double duracaoTotalRevelacao() {
        return ( TITULO.length() + NOMES.length() ) * ATRASO_POR_LETRA + DURACAO_LETRA;
    }

    public static void main( String[] args ) {
        new Main();
    }
}