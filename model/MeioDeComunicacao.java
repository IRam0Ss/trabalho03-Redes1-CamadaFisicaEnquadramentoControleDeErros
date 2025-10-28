package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;
import java.util.Random;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Simula a transmissao de forma otimizada em uma unica passada,
 * aplicando o erro de um bit por quadro e exibindo o resultado em um Alert.
 */
public class MeioDeComunicacao {

  /**
   * contrutor da classe responsavel por simular a transferecia bit a bit e o erro
   * * @param fluxoBrutoDeBits fluxoBruto de bits que represneta o sinal
   * codificado
   * pela camada anterior
   */
  public MeioDeComunicacao(int fluxoBrutoDeBits[]) {

    ControlerTelaPrincipal controller = ControlerTelaPrincipal.controlerTelaPrincipal;
    double taxaErro = controller.getValorTaxaErro();
    Random random = new Random();

    int[] fluxoBrutoDeBitsPontoA = fluxoBrutoDeBits;
    int[] fluxoBrutoDeBitsPontoB = new int[fluxoBrutoDeBitsPontoA.length]; // array de destino tem o mesmo tamanho do
                                                                           // array partida

    int totalDeBits = ManipulacaoBits.descobrirTotalDeBitsReais(fluxoBrutoDeBitsPontoA);
    int tipoDeEnquadramento = controller.opcaoEnquadramentoSelecionada();
    int tipoDeCodificacao = controller.opcaoSelecionada();

    // um strigBuider pra construir o relatorio de erro
    StringBuilder relatorio = new StringBuilder();
    relatorio.append("Taxa de Erro configurada: ").append(String.format("%.1f%%", taxaErro * 100))
        .append(" por quadro.\n");
    relatorio.append("Iniciando transferência otimizada de ").append(totalDeBits).append(" bits...\n\n");

    // supor o tamanho dos quadros fisicos, baseado na codificacao feita
    int tamanhoLogicoDoQuadroEmBits;
    switch (tipoDeEnquadramento) {
      case 0:
        // Contagem de caracteres - assumindo max de 36 bits de dados (4 bytes) + 1
        // byte de cabecalho
        // 4 bytes * 9 bits (paridade) = 36. Cabecalho = 8 bits. Total 44 bits.
        // Vamos usar uma media de 40 bits
        tamanhoLogicoDoQuadroEmBits = 40;
        break;
      case 1: // insersao de bytes tem tamanho variavel
      case 2: // insersao de bits tem tamanho variavel
        tamanhoLogicoDoQuadroEmBits = 32; // usa uma abstracao, aplicando chance de erro a cada 32 bits
        break;
      case 3:
        tamanhoLogicoDoQuadroEmBits = 36; // 32 carga util mais 4 bits 1111 de flag
        break;
      default:
        tamanhoLogicoDoQuadroEmBits = totalDeBits;
        break;
    } // fim switch

    int tamanhoFisicoDoQuadroEmBits = tamanhoLogicoDoQuadroEmBits;
    if (tipoDeCodificacao == 1 || tipoDeCodificacao == 2) {
      tamanhoFisicoDoQuadroEmBits *= 2;
    } // fim do if

    // simulacao da transferencia bit a bit
    int posicaoDoErroNesteQuadro = -1; // -1 significa que nao ha erro planejado
    int contadorDeErros = 0;

    for (int i = 0; i < totalDeBits; i++) {

      // no inicio de cada quadro, decide se havera um erro
      if (tamanhoFisicoDoQuadroEmBits > 0 && i % tamanhoFisicoDoQuadroEmBits == 0) {
        posicaoDoErroNesteQuadro = -1; // reseta o erro do quadro anterior

        // sorteia se o quadro ATUAL tera um erro
        if (random.nextDouble() < taxaErro) {
          // sorteia a POSICAO do erro dentro do quadro
          int bitAleatorioNoQuadro = random.nextInt(tamanhoFisicoDoQuadroEmBits);
          // calcula a posicao global onde o erro acontecera
          posicaoDoErroNesteQuadro = i + bitAleatorioNoQuadro;
          // Garante que o erro nao caia fora do total de bits
          if (posicaoDoErroNesteQuadro >= totalDeBits) {
            posicaoDoErroNesteQuadro = totalDeBits - 1;
          }
        }
      }

      // le o bit do A
      int bit = ManipulacaoBits.lerBits(fluxoBrutoDeBitsPontoA, i, 1);

      // verifica se este bit deve ser corrompido
      if (i == posicaoDoErroNesteQuadro) {
        // se for, inverte o bit
        bit = 1 - bit;

        contadorDeErros++;
        relatorio.append("-> Erro inserido no bit de índice: ").append(i).append("\n");
      }

      // escreve o bit no B
      ManipulacaoBits.escreverBits(fluxoBrutoDeBitsPontoB, i, bit, 1);

    } // fim for

    relatorio.append("\nTransferência concluída.");
    relatorio.append("\nTotal de bits corrompidos = " + contadorDeErros);

    
    System.out.println("--- RELATORIO DO MEIO DE COMUNICACAO (DEBUG) ---");
    System.out.println(relatorio.toString());

    controller.desenharSinalTransmissao(ManipulacaoBits.desempacotarBits(fluxoBrutoDeBitsPontoA, totalDeBits));
    new CamadaFisicaReceptora(fluxoBrutoDeBitsPontoB);
  } // fim do MeioComunicacao

  /**
   * Metodo para exibir aletrta e relatorio
   * 
   * @param contadorDeErros quantidade de erros ocorridos
   * 
   * @param mensagem        mensagem a ser exibvida
   */
  private void exibirAlerta(int contadorDeErros, String mensagem) {
    Platform.runLater(() -> {
      Alert alert;
      if (contadorDeErros == 0) {
        alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Resultado da Transmissão");
        alert.setHeaderText("SUCESSO!");
        alert.setContentText("A mensagem foi transmitida sem a ocorrência de erros.");
      } else {
        alert = new Alert(AlertType.WARNING);
        alert.setTitle("Resultado da Transmissão");
        alert.setHeaderText("OCORRÊNCIA DE ERRO!");
        alert.setContentText("A mensagem foi corrompida durante o percurso. Veja o relatório abaixo.");
      }

      javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(mensagem);
      textArea.setEditable(false);
      textArea.setWrapText(true);

      alert.getDialogPane().setExpandableContent(textArea);
      alert.getDialogPane().setExpanded(true);

      alert.showAndWait();
    });
  }// fim do exibirAlerta

} // fim da classe