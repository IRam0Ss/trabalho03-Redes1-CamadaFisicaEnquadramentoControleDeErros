package model;

import controller.ControlerTelaPrincipal;

/**
 * classe que representa a aplicacao do sistema receptor
 */
public class AplicacaoReceptora {

  /**
   * o contrutor da classe que eh responsavel por exibir na tela a mensagem que
   * chegou ate essa camada
   * 
   * @param mensagem mensagem que chegou ao receptor
   */
  public AplicacaoReceptora(String mensagem) {

    ControlerTelaPrincipal.controlerTelaPrincipal.exibirMensagemRecebida(mensagem); // exibe a mensagem recebida

  }// fim do contrutor

} // fim da classe
