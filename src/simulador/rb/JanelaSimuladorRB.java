package simulador.rb;

import simulador.arvore.ArvoreBase;
import simulador.arvore.JanelaSimuladorArvore;

/**
 * Simulador da Rubro-Negra. A opção "Visão 2-3" fica dentro do painel do modo
 * didático; ao mudar, a árvore inteira é reorganizada.
 */
public class JanelaSimuladorRB extends JanelaSimuladorArvore {

    private boolean visao23;

    public JanelaSimuladorRB() {
        super( "Simulador: Árvore Rubro-Negra" );
    }

    @Override
    public void create() {
        super.create();
        modoDidatico().suportarExtra( true );
    }

    @Override
    protected ArvoreBase criarArvore() {
        return new RubroNegra();
    }

    @Override
    protected void atualizarExtras( double delta ) {
        boolean novo = modoDidatico().extraAtiva();
        if ( novo != visao23 ) {
            visao23 = novo;
            RubroNegra rb = (RubroNegra) arvore;
            rb.setModo23( visao23 );
            rb.reorganizar();
        }
    }
}