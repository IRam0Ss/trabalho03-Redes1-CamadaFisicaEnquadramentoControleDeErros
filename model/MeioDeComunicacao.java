package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;
import java.util.Random;

/**
 * Simula a transmissao de forma otimizada em uma unica passada,
 * aplicando o erro de um bit por quadro e exibindo o resultado em um Alert.
 */
public class MeioDeComunicacao {

  // referncias para os dois lados da comunicacao
  private CamadaFisicaReceptora receptor;
  private CamadaFisicaTransmissora transmissor;
  private ControlerTelaPrincipal controlerTelaPrincipal;
  private Random random;

  /**
   * construtor da classe
   * 
   * @param transmissor referencia para o transmissor da mensagem
   * @param receptor    referencia para o receptor da mensagem
   */
  public MeioDeComunicacao(CamadaFisicaTransmissora transmissor, CamadaFisicaReceptora receptor,
      ControlerTelaPrincipal controlerTelaPrincipal) {
    this.transmissor = transmissor;
    this.receptor = receptor;
    this.controlerTelaPrincipal = controlerTelaPrincipal;
    this.random = new Random();
  } // fim construtor

  /**
   * metodo principal que simula a transmissao da mensagem
   * 
   * @param fluxoBrutoDeBits fluxoBruto de bits que represneta o sinal
   *                         codificado
   *                         pela camada anterior
   */
  public void transmitirMensagem(int fluxoBrutoDeBits[]) {

    double taxaErro = this.controlerTelaPrincipal.getValorTaxaErro();

    int[] fluxoBrutoDeBitsPontoA = fluxoBrutoDeBits;
    int[] fluxoBrutoDeBitsPontoB = new int[fluxoBrutoDeBitsPontoA.length]; // array de destino tem o mesmo tamanho do
                                                                           // array partida

    int totalDeBits = ManipulacaoBits.descobrirTotalDeBitsReais(fluxoBrutoDeBitsPontoA);
    int tipoDeEnquadramento = this.controlerTelaPrincipal.opcaoEnquadramentoSelecionada();
    int tipoDeCodificacao = this.controlerTelaPrincipal.opcaoSelecionada();

    // um strigBuider pra construir o relatorio de erro (debug)
    StringBuilder relatorio = new StringBuilder();
    relatorio.append("Taxa de Erro configurada: ").append(String.format("%.1f%%", taxaErro * 100))
        .append(" por quadro.\n");
    relatorio.append("Iniciando transferência otimizada de ").append(totalDeBits).append(" bits...\n\n");

    // supor o tamanho dos quadros fisicos, baseado na codificacao feita
    int tamanhoLogicoDoQuadroEmBits;
    switch (tipoDeEnquadramento) {
      case 0:        
      case 1:
      case 2: 
        tamanhoLogicoDoQuadroEmBits = 40;
        break;
      case 3:
        tamanhoLogicoDoQuadroEmBits = 36;
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
          } // fim if
        } // fim if
      } // fim if

      // le o bit do A
      int bit = ManipulacaoBits.lerBits(fluxoBrutoDeBitsPontoA, i, 1);

      // verifica se este bit deve ser corrompido
      if (i == posicaoDoErroNesteQuadro) {
        // se for, inverte o bit
        bit = 1 - bit;

        contadorDeErros++;
        relatorio.append("-> Erro inserido no bit de índice: ").append(i).append("\n");
      } // fim if

      // escreve o bit no B
      ManipulacaoBits.escreverBits(fluxoBrutoDeBitsPontoB, i, bit, 1);

    } // fim for

    relatorio.append("\nTransferência concluída.");
    relatorio.append("\nTotal de bits corrompidos = " + contadorDeErros);

    System.out.println("--- RELATORIO DO MEIO DE COMUNICACAO (DEBUG) ---");
    System.out.println(relatorio.toString());

    this.controlerTelaPrincipal
        .desenharSinalTransmissao(ManipulacaoBits.desempacotarBits(fluxoBrutoDeBitsPontoA, totalDeBits));
    this.receptor.receberQuadro(fluxoBrutoDeBitsPontoB);
  } // fim do MeioComunicacao

} // fim da classe