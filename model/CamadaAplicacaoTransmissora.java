package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;

/**
 * classe responsavel por simular a funcao da camada de aplicacao transmissora
 * de um sistema conectado a rede
 */
public class CamadaAplicacaoTransmissora {

  public CamadaAplicacaoTransmissora() {
  } 

  /**
   * trabalhando com construtor da classe, esssa classe eh responsavel por
   * realizar a conversao inicial, de String para um array de int
   * 
   * @param mensagem o texto digitado pelo usuario, recebido da camada anterio,
   *                 que sera convertido em int[] e enviado para a proxima camada
   */
  public CamadaAplicacaoTransmissora(String mensagem) {

    int[] quadro = ManipulacaoBits.stringParaIntAgrupado(mensagem); // converte a mensagem para binario

    ControlerTelaPrincipal.controlerTelaPrincipal.exibirRepresentMensagemBinariaTransmitida(quadro); // mostra o binario na tela 

    new CamadaEnlaceDadosTransmissora(quadro); // envia para a proxima camada a mensagem convertida para binario 
  } // fim do construtor 

} // fim da classe CamadaAplicacaoTransmissora
