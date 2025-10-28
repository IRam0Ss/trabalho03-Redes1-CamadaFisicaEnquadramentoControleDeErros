package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;

/**
 * classe responsavel por simular a funcao da camada de aplicacao do sistema
 * receptor, basicamente recebe a mensagem decodificada em sua versao binaria e
 * a converte para a tring equivalente
 */
public class CamadaAplicacaoReceptora {

  public CamadaAplicacaoReceptora() {
  }

  /**
   * construtor da classe responsavel por converter a mensagem em binario de volta
   * para string
   * 
   * @param quadro a mensagem recebida decodificada pela camada fisica, na forma
   *               de binario
   */
  public CamadaAplicacaoReceptora(int[] quadro) {

    ControlerTelaPrincipal.controlerTelaPrincipal.exibirRepresentMensagemBinariaRecebida(quadro); // mostra a forma
                                                                                                  // binaria da mensagem
                                                                                                  // recebida

    String mensagem = ManipulacaoBits.intAgrupadoParaString(quadro, (32 * quadro.length)); // traduz o binario de volta
                                                                                           // para String

    new AplicacaoReceptora(mensagem);

  }// fim construtor

} // fim da classe