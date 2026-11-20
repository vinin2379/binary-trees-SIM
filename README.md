# Simulador de Árvores Binárias 🌳

Simulador visual, em Java, de **Árvore Binária de Busca (ABB)** e **Árvore AVL**. Cada inserção, remoção e busca aparece animada na tela, com um **modo didático** que explica o que está acontecendo em cada passo. Feito sobre a biblioteca **JSGE**.

## Contexto

Projeto desenvolvido na disciplina, com a biblioteca **JSGE (Java Simple Game Engine)**, criada pelo professor David Buzatto, inspirada na Raylib e adaptada para Java.

## O que o simulador faz

- Inserir, remover e buscar valores em uma ABB.
- Inserir e remover valores em uma AVL, com as **rotações** (simples e duplas) mostradas na animação.
- Animação de percurso: um "viajante" desce pela árvore até o nó procurado.
- **Modo didático**: destaca o caminho percorrido e mostra o motivo de cada decisão (menor ou maior que o nó atual).
- Efeitos sonoros e tema visual próprio.

## Estrutura

```
src/simulador/
├── arvore/        → classes base comuns (ArvoreBase, No)
├── abb/           → ABB, Viajante e janela do simulador de ABB
├── avl/           → AVL e janela do simulador de AVL
└── ui/            → componentes visuais (botões, campo de texto, tema, sons, modo didático)
```

A lógica de cada árvore fica separada da interface. A `ABB` implementa apenas inserção e remoção, e herda o restante da `ArvoreBase`.

## Como rodar

O projeto usa **NetBeans** (há a pasta `nbproject/`):

1. Abra a pasta do projeto no NetBeans.
2. Confirme que a biblioteca JSGE está em `lib/`.
3. Execute a janela principal do simulador.

Requisitos: JDK 8 ou superior.

## Tecnologias

Java · JSGE (Java Simple Game Engine) · Swing/AWT · NetBeans
