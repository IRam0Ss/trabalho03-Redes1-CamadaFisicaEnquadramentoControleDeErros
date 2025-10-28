package model;

import controller.ControlerTelaPrincipal;
import util.ManipulacaoBits;

public class CamadaEnlaceDadosReceptora {

  /**
   * construtor, responsavel por desenquadrar os quadros recebidos e decodificados
   * da camada fisica e enviar para a proxima camada
   * 
   * @param quadro quadro recebido da camada anterior
   */
  public CamadaEnlaceDadosReceptora(int quadro[]) {

    int[] quadroDesenquadrado, quadroVerificado;

    quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramento(quadro);
    quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErro(quadroDesenquadrado);
    // CamadaEnlaceDadosReceptoraControleDeFluxo(quadro);

    // chama proxima camada

    new CamadaAplicacaoReceptora(quadroVerificado);
  }// fim do metodo CamadaEnlaceDadosReceptora

  /**
   * metodo que escolhe o tipo de desenquadramento a ser aplicado na mensagem
   * 
   * @param quadro mensagem recebida da camada anterior com os bits enquadrados
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramento(int quadro[]) {
    int tipoDeEnquadramento = ControlerTelaPrincipal.controlerTelaPrincipal.opcaoEnquadramentoSelecionada();
    int[] quadroDesenquadrado = null;
    switch (tipoDeEnquadramento) {
      case 0: // contagem de caracteres
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: // insercao de bytes
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: // insercao de bits
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: // violacao da camada fisica
        quadroDesenquadrado = CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(quadro);
        break;
    }// fim do switch/case

    return quadroDesenquadrado; // retorna o quadro ja desenquadrado

  }// fim do metodo CamadaEnlaceDadosReceptoraEnquadramento

  public int[] CamadaEnlaceDadosReceptoraControleDeErro(int quadro[]) {
    int tipoDeControleDeErro = ControlerTelaPrincipal.controlerTelaPrincipal.opcaoControleErroSelecionada();
    int[] quadroVerificado = null;
    switch (tipoDeControleDeErro) {
      case 0: // paridade par
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(quadro);
        break;
      case 1: // paridade impar
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(quadro);
        break;
      case 2: // CRC
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroCRC(quadro);
        break;
      case 3: // hamming
        quadroVerificado = CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(quadro);
        break;
    }// fim do switch/case
    return quadroVerificado;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErro

  public void CamadaEnlaceDadosReceptoraControleDeFluxo(int quadro[]) {
    // algum codigo aqui
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeFluxo

  /**
   * metodo para desenquadrar o quadro utilizando o metodo de contagem de
   * caracteres
   * 
   * @param quadro quadro recebido com os bits enquadrados
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoContagemDeCaracteres(int quadro[]) {

    // usa o tamanho real do sinal para evitar ler lixo
    int maximoDeBitsNoQuadro = ManipulacaoBits.descobrirTotalDeBitsReais(quadro);
    if (maximoDeBitsNoQuadro == 0)
      return new int[0];

    // usa um buffer grande e depois apara para o tamanho exato
    int[] bufferTemporario = new int[quadro.length];
    int bitEscritaGlobal = 0;
    int bitLeituraGlobal = 0;

    while (bitLeituraGlobal < maximoDeBitsNoQuadro) { // loop enquanto tiver bits pra ler
      // garante que ha espaco para ler um cabecalho
      if (bitLeituraGlobal + 8 > maximoDeBitsNoQuadro)
        break;

      // le o cabecalho de 8 bits
      int contagem = ManipulacaoBits.lerBits(quadro, bitLeituraGlobal, 8);
      bitLeituraGlobal += 8;

      if (contagem == 0)
        break;

      // le a carga util
      int bitsCargaUtil = (contagem - 1) * 8; // calcula quantos bits serao de carga util

      // garante que a carga util nao ultrapassa o final do quadro
      if (bitLeituraGlobal + bitsCargaUtil > maximoDeBitsNoQuadro)
        break;

      int cargaUtil = ManipulacaoBits.lerBits(quadro, bitLeituraGlobal, bitsCargaUtil);
      bitLeituraGlobal += bitsCargaUtil;

      // escreve a carga util no buffer de saida
      ManipulacaoBits.escreverBits(bufferTemporario, bitEscritaGlobal, cargaUtil, bitsCargaUtil);
      bitEscritaGlobal += bitsCargaUtil;
    } // fim while

    // Cria o array final com o tamanho EXATO dos dados extraidos.
    int[] quadroDesenquadrado = new int[(bitEscritaGlobal + 31) / 32];
    for (int i = 0; i < bitEscritaGlobal; i++) {
      int bit = ManipulacaoBits.lerBits(bufferTemporario, i, 1);
      ManipulacaoBits.escreverBits(quadroDesenquadrado, i, bit, 1);
    } // fim for

    return quadroDesenquadrado;
  }// fim do metodo CamadaEnlaceDadosReceptoraContagemDeCaracteres

  /**
   * metodo para desenquadrar o quadro utilizando o metodo de insercao de bytes
   * 
   * @param quadro quadro recebido com os bits enquadrados
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBytes(int quadro[]) {

    final int FLAG = 0b01111110; // valor do byte de flag, o mesmo do transmissor
    final int SCAPE = 0b01111101; // valor do byte de escape, o mesmo do transmissor

    // primeiro, contamos o total de bytes validos recebidos
    int contadorBytesRecebidos = 0;
    boolean fimDados = false;
    for (int inteiroAgrupado : quadro) {
      if (fimDados)
        break;
      for (int i = 0; i < 4; i++) {
        int umByte = (inteiroAgrupado >> (24 - i * 8)) & 0xFF;
        if (umByte == 0) {
          fimDados = true;
          break;
        } // fim if
        contadorBytesRecebidos++;
      } // fim for
    } // fim do for

    // calcular quantos bytes de carga util vao existir apos desenquadramento
    int contadorBytesCargaUtil = 0;
    for (int i = 0; i < contadorBytesRecebidos; i++) {
      // extrai o byte da posicao atual para analise
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int byteAtual = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      if (byteAtual == FLAG) {
        // Flags nao contam como carga util
        continue;
      }

      if (byteAtual == SCAPE) {
        // O SCAPE eh ignorado, mas o proximo byte conta como carga util.
        // Avanca o 'i' para pular o proprio SCAPE na contagem.
        i++;
        contadorBytesCargaUtil++;
      } else {
        // Se nao for FLAG nem SCAPE, eh um byte de carga util
        contadorBytesCargaUtil++;
      } // fim if/ else
    } // fim for

    // cria o array final com o tamanho exato que foi calculado
    int tamanhoQuadroDesenquadrado = (contadorBytesCargaUtil + 3) / 4;
    int[] quadroDesenquadrado = new int[tamanhoQuadroDesenquadrado];

    // indice para controlar em qual bit do quadro final o proximo byte sera escrito
    int indiceBitDestino = 0;

    // percorre os bytes recebidos novamente, desta vez para extrair e escrever a
    // carga util
    for (int i = 0; i < contadorBytesRecebidos; i++) {
      // extrai o byte da posicao atual
      int indiceInteiro = i / 4;
      int posicaoNoInteiro = i % 4;
      int byteAtual = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

      if (byteAtual == FLAG) {
        // se o byte for flag, ignora
        continue;
      } // fim if

      if (byteAtual == SCAPE) {
        // se o byte eh um scape, avanca para o proximo byte
        i++;
        // extrai o byte de dados que vem apos o SCAPE
        indiceInteiro = i / 4;
        posicaoNoInteiro = i % 4;
        int byteDeDados = (quadro[indiceInteiro] >> (24 - posicaoNoInteiro * 8)) & 0xFF;

        // escreve o byte de dados no quadro final
        ManipulacaoBits.escreverBits(quadroDesenquadrado, indiceBitDestino, byteDeDados, 8);
        indiceBitDestino += 8; // avanca o ponteiro de escrita em 8 bits
      } else {
        // se passou as verificacoes, o byte eh carga util
        // escreve o byte de dados no quadro final
        ManipulacaoBits.escreverBits(quadroDesenquadrado, indiceBitDestino, byteAtual, 8);
        indiceBitDestino += 8; // avanca o ponteiro de escrita em 8 bits
      } // fim if/else
    } // fim for

    return quadroDesenquadrado;
  }// fim do metodo CamadaEnlaceDadosReceptoraInsercaoDeBytes

  /**
   * metodo para desenquadrar o quadro utilizando a insercao de bits
   * 
   * @param quadro quadro recebido enquadrado
   * @return o quadro ja desenquadrado
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits(int quadro[]) {

    final int FLAG = 0b01111110; // O mesmo valor de flag do transmissor

    int tamanhoMaximoEstimado = quadro.length * 32; // define o valor maximo possivel
    int[] bufferTemporario = new int[tamanhoMaximoEstimado];
    int indiceBitDestino = 0;
    int contadorBitsUm = 0;
    boolean inicioQuadro = false;

    // itera por todos os bits do quadro recebido
    for (int i = 0; i < tamanhoMaximoEstimado; i++) {
      // leitura de um bit do quadro de entrada
      int indiceInteiro = i / 32;
      int posicaoNoInteiro = 31 - (i % 32);
      int bitAtual = (quadro[indiceInteiro] >> posicaoNoInteiro) & 1;

      // dtecta inicio de quaro descarta flag
      if (!inicioQuadro) {
        // procura pela primeira flag para começar o processamento
        if (i + 7 < tamanhoMaximoEstimado) {
          int possivelFlag = ManipulacaoBits.lerBits(quadro, i, 8);
          if (possivelFlag == FLAG) {
            inicioQuadro = true;
            i += 7; // Pula os bits da flag que já foram lidos
            continue; // Volta ao início do loop
          } // fim if
        } // fim if
        continue; // Se não for uma flag, continua procurando
      } // fim if

      // comeca a processar os dados
      // verifica se os bits formam flag
      if (i + 7 < tamanhoMaximoEstimado) {
        int possivelFlag = ManipulacaoBits.lerBits(quadro, i, 8);
        if (possivelFlag == FLAG) {
          // Encontrou uma flag, seja intermediária ou final
          i += 7; // Pula os bits da flag
          contadorBitsUm = 0; // Reseta o contador de '1's
          continue; // Continua para o próximo bit após a flag
        } // fim if
      } // fi if

      if (contadorBitsUm == 5) {
        // bit adicionado pelo stuffing eh descartado
        if (bitAtual == 0) {
          contadorBitsUm = 0;
          continue; // descarta o bit '0' e continua o loop
        } // fim if
      } // fimif

      // escreve o bit de dado no buffer temporario
      ManipulacaoBits.escreverBits(bufferTemporario, indiceBitDestino, bitAtual, 1);
      indiceBitDestino++;

      // atualiza o contador de bits '1'
      if (bitAtual == 1) {
        contadorBitsUm++;
      } else {
        contadorBitsUm = 0;
      } // fim if/else
    } // fim for

    // cria array final com tamanho exato
    int tamanhoArrayFinal = (indiceBitDestino + 31) / 32;
    int[] quadroDesenquadrado = new int[tamanhoArrayFinal];

    // Copia os bits do buffer temporário para o array final
    for (int i = 0; i < indiceBitDestino; i++) {
      int bit = ManipulacaoBits.lerBits(bufferTemporario, i, 1);
      ManipulacaoBits.escreverBits(quadroDesenquadrado, i, bit, 1);
    } // fim for

    return quadroDesenquadrado;
  } // fim do metodo CamadaEnlaceDadosReceptoraEnquadramentoInsercaoDeBits

  /**
   * Passa somente o quadro pra proxima camada, pois a responsabilidade de
   * dosenquadramento foi violada e colocada pra camada fisica
   * 
   * @param quadro o quadro ja desenquadrado
   * @return o mesmo quadro
   */
  public int[] CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica(int quadro[]) {

    return quadro;
  } // fim CamadaEnlaceDadosReceptoraEnquadramentoViolacaoDaCamadaFisica

  public int[] CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadePar

  public int[] CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroBitDeParidadeImpar

  public int[] CamadaEnlaceDadosReceptoraControleDeErroCRC(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroCRC

  public int[] CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming(int quadro[]) {
    // algum codigo aqui
    return quadro;
  }// fim do metodo CamadaEnlaceDadosReceptoraControleDeErroCodigoDeHamming

}// fim da classe CamadaEnlaceDadosReceptora
