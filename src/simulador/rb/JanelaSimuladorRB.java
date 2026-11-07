package simulador.rb;

import br.com.davidbuzatto.jsge.math.Vector2;
import simulador.arvore.ArvoreBase;
import simulador.arvore.JanelaSimuladorArvore;
import simulador.ui.BotaoEstilizado;
import simulador.ui.Tema;

/**
 * Simulador da Rubro-Negra, com o botão que alterna para a visão 2-3.
 */
public class JanelaSimuladorRB extends JanelaSimuladorArvore {

    private BotaoEstilizado btnVisao;

    public JanelaSimuladorRB() {
        super( "Simulador: Árvore Rubro-Negra" );
    }

    @Override
    public void create() {
        super.create();
        btnVisao = new BotaoEstilizado( 10, 520, 130, 32, "Visão 2-3",
                Tema.ITEM, Tema.ITEM_HOVER, Tema.TEXTO );
    }

    @Override
    protected ArvoreBase criarArvore() {
        return new RubroNegra();
    }

    @Override
    protected void atualizarExtras( double delta ) {
        RubroNegra rb = (RubroNegra) arvore;
        if ( btnVisao.atualizar( this ) ) {
            rb.setModo23( !rb.modo23() );
            rb.reorganizar();
        }
    }

    @Override
    protected boolean sobExtras( Vector2 m ) {
        return m.x >= 10 && m.x <= 140 && m.y >= 520 && m.y <= 552;
    }

    @Override
    protected void desenharExtras() {
        btnVisao.desenhar( this );
    }
}