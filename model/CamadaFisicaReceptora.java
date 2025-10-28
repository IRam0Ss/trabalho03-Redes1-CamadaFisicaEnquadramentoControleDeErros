package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;

/**
 * camada responsavel por simular a camada fisica do sistema receptor recebe a
 * mensagem vinda codificada na forma de sinal do meio de comulicacao e
 * descodifica ela
 */
public class CamadaFisicaReceptora {

  /**
   * construtor da classe responsavel por receber a mensagem codificada e
   * decodificasr de acordo com o metodo que foi selecionado
   * 
   * @param quadro a mensagem codificada
   */
  public CamadaFisicaReceptora(int[] quadro) {

    ControlerTelaPrincipal.controlerTelaPrincipal.exibirRepresentSinalRecebido(quadro); // exibe a representacao do
                                                                                        // sinal recebido
    int tipoDeEnquadramento = ControlerTelaPrincipal.controlerTelaPrincipal.opcaoEnquadramentoSelecionada();
    int tipoDeDecodificacao = ControlerTelaPrincipal.controlerTelaPrincipal.opcaoSelecionada();
    int fluxoBrutoDeBits[] = null;

    if (tipoDeEnquadramento == 3) {
      fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoComViolacao(quadro, tipoDeDecodificacao);
    } else {
      switch (tipoDeDecodificacao) {
        case 0: // codificao binaria
          fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoBinaria(quadro);
          break;
        case 1: // codificacao manchester
          fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoManchester(quadro);
          break;
        case 2: // codificacao manchester diferencial
          fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoManchesterDiferencial(quadro);
          break;
      }// fim do switch/case
    } // fim if/else
      // chama proxima camada

    new CamadaEnlaceDadosReceptora(fluxoBrutoDeBits);
  }// fim do metodo CamadaFisicaTransmissora

  /**
   * metodo para decodificar o Binario, basicamente retorna o mesmo array
   * 
   * @param quadro conjunto de bits recebido
   * @return o conjunto de bits decodificado
   */
  public int[] CamadaFisicaReceptoraDecodificacaoBinaria(int[] quadro) {
    return quadro;
  } // fim do metodo

  /**
   * decodifica o codigo manchester
   * 10 -> 1
   * 01 -> 0
   * 
   * @param quadro array com bits codificados em manchester
   * @return array decodificado
   */
  public int[] CamadaFisicaReceptoraDecodificacaoManchester(int[] quadro) {

    int totalBitsManchester = ManipulacaoBits.descobrirTotalDeBitsReais(quadro); // descobre quantos bits tem a mensagem
                                                                                 // recebida
    int totalBitsOriginal = totalBitsManchester / 2; // calcula quantos bits tem a mensagem original e por consequencia
                                                     // tera a mensagem decodificada
    int tamanhoArrayDecodificado = (totalBitsOriginal + 31) / 32;
    int[] mensagemDecodificada = new int[tamanhoArrayDecodificado];

    // itera os bits em pares, uma vez que a cada 2 bits do manchester tem 1 bit do
    // original
    for (int i = 0; i < totalBitsManchester; i += 2) {

      // le e recupera o bit1 do par
      int indiceBit1 = i / 32;
      int posicaoBit1 = 31 - (i % 32);
      int bit1 = (quadro[indiceBit1] >> posicaoBit1) & 1;

      // le e recupera o bit2 do par
      int indiceBit2 = (i + 1) / 32;
      int posicaoBit2 = 31 - ((i + 1) % 32);
      int bit2 = (quadro[indiceBit2] >> posicaoBit2) & 1;

      // determina qual o bit original
      int bitOriginal = 0;
      if (bit1 == 1 && bit2 == 0) { // se o par de bit for 10 entao o bit original eh 1, caso contrario ele eh 0
        bitOriginal = 1;
      }

      // escreve o bit no array de retorno
      if (bitOriginal == 1) {
        int posicaoGlobalOriginal = i / 2; // mapea a posicao do fluxo manchester de volta na posicao original
        int indiceOriginal = posicaoGlobalOriginal / 32;
        int posicaoOriginal = 31 - (posicaoGlobalOriginal % 32);
        mensagemDecodificada[indiceOriginal] = mensagemDecodificada[indiceOriginal] | (1 << posicaoOriginal);
      }

    }
    return mensagemDecodificada;
  }// fim do metodo

  /**
   * decodifica o manchester diferencial
   * primeiro bit eh o padrao, ja os proximos bits sao traduzidos a partir da
   * existencia de transicao e a nao existencia
   * sem transicao -> 1
   * com transicao -> 0
   * 
   * @param quadro o bacote de bits codificado em manchester diferencial
   * @return o pacote de inteiros decodificado, com a informacao da mensagem em
   *         binario
   */
  public int[] CamadaFisicaReceptoraDecodificacaoManchesterDiferencial(int[] quadro) {

    int totalBitsDiferencial = ManipulacaoBits.descobrirTotalDeBitsReais(quadro); // descobre quantos bits tem a
                                                                                  // mensagem recebida
    int totalBitsOriginal = totalBitsDiferencial / 2; // calcula quantos bits tem a mensagem original e por consequencia
                                                      // tera a mensagem decodificada
    int tamanhoArrayDecodificado = (totalBitsOriginal + 31) / 32;
    int[] mensagemDecodificada = new int[tamanhoArrayDecodificado];

    int nivelAnterior = 1; // variavel padronizada para iniciar a leitura, TEM que ser igual ao
                           // "nivelAtual" do transmissor

    // percorre os bits codificados em pares
    for (int i = 0; i < totalBitsDiferencial; i += 2) {

      // leitura da primeira parte do sinal
      int posicaoGeralSinal1 = i;
      int indiceSinal1 = posicaoGeralSinal1 / 32;
      int posicaoSinal1 = 31 - (posicaoGeralSinal1 % 32);
      int primeiroNivelSinal = (quadro[indiceSinal1] >> posicaoSinal1) & 1; // le no array o sinal1 e o armazena

      // define o bit original comparando o nivel do sinal encontrado com o anterior
      int bitOriginal;
      if (primeiroNivelSinal == nivelAnterior) { // se o sinal se manteve, nao ouve transicao logo bit = 1
        bitOriginal = 1;
      } else { // caso contrario ouve transicao logo bit = 0
        bitOriginal = 0;
      }

      // escreve o bit decodificado no array de decodificacao
      if (bitOriginal == 1) {
        int posicaoGlobalOriginal = i / 2; // mapea a posicao do fluxo diferencil de volta na posicao original
        int indiceOriginal = posicaoGlobalOriginal / 32;
        int posicaoOriginal = 31 - (posicaoGlobalOriginal % 32);
        mensagemDecodificada[indiceOriginal] = mensagemDecodificada[indiceOriginal] | (1 << posicaoOriginal);
      }

      // atualiza o nivel anterior como a segunda parte do sinal, uma vez que eh a
      // aprtir dele que se sabe se ouvbe ou nao transicao
      int posicaoGeralSinal2 = i + 1;
      int indiceSinal2 = posicaoGeralSinal2 / 32;
      int posicaoSinal2 = 31 - (posicaoGeralSinal2 % 32);
      nivelAnterior = (quadro[indiceSinal2] >> posicaoSinal2) & 1; // le o sinal2 ou seja a segunda parte do sinal e
      // armazena no nivel anterior

    }

    return mensagemDecodificada;
  }// fim do metodo

  /**
   * Encontra os marcadores de violacao (11), decodifica os dados entre eles
   * e entrega o quadro limpo.
   * 
   * @param quadro              O sinal bruto vindo do meio fisico.
   * @param tipoDeDecodificacao A decodificacao a ser utilizada.
   * @return O quadro de dados decodificado e desenquadrado.
   */
  private int[] CamadaFisicaReceptoraDecodificacaoComViolacao(int[] quadro, int tipoDeDecodificacao) {

    final int VIOLACAO = 0b1111;
    final int TAMANHO_VIOLACAO_BITS = 4;

    int totalBitsSinal = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
    if (totalBitsSinal == 0)
      return new int[0];

    int[] quadroDecodificado = new int[quadro.length]; // buffer temporario
    int bitEscritaGlobal = 0;
    boolean quadroIniciado = false;
    int nivelAnterior = 1; // para Manchester Diferencial

    int i = 0; // usando while para melhor controle do indice manualmente
    while (i <= totalBitsSinal - TAMANHO_VIOLACAO_BITS) { // garante que pelo menos a chance de leitura de 1 flag

      // verifica se ha uma VIOLACAO (1111) na posicao atual

      int possivelViolacao = ManipulacaoBits.lerBits(quadro, i, TAMANHO_VIOLACAO_BITS);
      if (possivelViolacao == VIOLACAO) {
        quadroIniciado = true; // marca que o processamento de dados pode comecar
        i += TAMANHO_VIOLACAO_BITS; // pula os 4 bits da violacao

        nivelAnterior = 1;

        if (i >= totalBitsSinal) { // se nao tem mais bits a verificar para o loop
          break;
        }

        continue; // volta ao inicio do loop para processar o que vem depois
      } // fim if
      // fim if

      // se o quadro ainda nao foi iniciado, simplesmente avanca, ignorando qualquer
      // "ruido" inicial.
      if (!quadroIniciado) {
        i++;
        continue;
      } // fim do if

      // decodifica os dados
      int bit1 = ManipulacaoBits.lerBits(quadro, i, 1);
      int bit2 = ManipulacaoBits.lerBits(quadro, i + 1, 1);

      int bitOriginal = 0;
      if (tipoDeDecodificacao == 1) { // Manchester
        if (bit1 == 1 && bit2 == 0) { // 10->1
          bitOriginal = 1;
        }
      } else { // Manchester Diferencial
        if (bit1 == nivelAnterior) { // sem transicao -> 1
          bitOriginal = 1;
        }
        nivelAnterior = bit2; // atualiza o nivel para a proxima comparacao
      }

      ManipulacaoBits.escreverBits(quadroDecodificado, bitEscritaGlobal++, bitOriginal, 1);

      i += 2; // avanca para o proximo par de bits do sinal

    } // fim while

    // ajusta o array final para o tamanho exato dos bits decodificados
    int tamanhoFinalArray = (bitEscritaGlobal + 31) / 32;
    int[] resultadoFinal = new int[tamanhoFinalArray];
    for (int j = 0; j < bitEscritaGlobal; j++) {
      int bit = ManipulacaoBits.lerBits(quadroDecodificado, j, 1);
      ManipulacaoBits.escreverBits(resultadoFinal, j, bit, 1);
    }
    return resultadoFinal;

  } // fim metodo CamadaFisicaReceptoraDecodificacaoComViolacao

}// fim da classe
