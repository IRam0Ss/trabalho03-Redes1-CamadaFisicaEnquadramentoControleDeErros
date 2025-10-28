package model;

import controller.ControlerTelaPrincipal;

/**
 * classe responsavel pela aplicacao do sistema que tranmite a mensagem
 */
public class AplicacaoTransmissora {

  /**
   * construtor da classe responsavel por pegar a mensagem digitada pelo usuario e
   * mandar para a proxima camada
   */
  public AplicacaoTransmissora() {

    if (ControlerTelaPrincipal.controlerTelaPrincipal.getCaixaTextoTransmitido() != null) {
      String mensagem = ControlerTelaPrincipal.controlerTelaPrincipal.getCaixaTextoTransmitido();
      new CamadaAplicacaoTransmissora(mensagem); // chama a proxima camada
    } else {
      System.out.println("DIGITE ALGO ");
    }
  } // fim do construtor
} // fim da classe
