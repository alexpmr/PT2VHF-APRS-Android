package com.pt2vhf.aprs.data

/*
 * #64: quando acompanhar automaticamente a mensagem nova numa lista que já está na tela.
 * Lógica pura para testar sem Compose; a UI só lê o estado da lista e decide rolar ou mostrar o aviso.
 */

/**
 * Conversa ou chat de grupo (mais nova embaixo): rola até a nova se a pessoa já estava no fim
 * (último item visível entre os dois últimos da contagem anterior), se a lista estava vazia
 * ou se a mensagem nova foi enviada por ela mesma. Caso contrário, mostra "nova mensagem ↓".
 */
fun followNewestAtEnd(lastVisibleIndex: Int, previousCount: Int, newestIsMine: Boolean): Boolean =
    newestIsMine || previousCount <= 0 || lastVisibleIndex >= previousCount - 2

/** Lista de conversas (mais recente no topo): acompanha se a pessoa estava no topo. */
fun followNewestAtTop(firstVisibleIndex: Int): Boolean = firstVisibleIndex <= 1
