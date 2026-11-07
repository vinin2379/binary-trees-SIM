package simulador.abb;

import simulador.arvore.ArvoreBase;
import simulador.arvore.JanelaSimuladorArvore;

/**
 * Simulador de ABB. Toda a lógica de janela está em JanelaSimuladorArvore.
 */
public class JanelaSimuladorABB extends JanelaSimuladorArvore {

    public JanelaSimuladorABB() {
        super( "Simulador: Árvore Binária de Busca (ABB)" );
    }

    @Override
    protected ArvoreBase criarArvore() {
        return new ABB();
    }
}