package util;

public class Auxiliar {

	// ideia inicial, mas tem que lembrabr de trabalhar com contagem de bits, pois
	// cada int do vetor pode ter uma parte que faz parte de outro quadro
	public static int[][] separarEmQuadros(int[] quadroEnquadrado, int tamanhoDoQuadro) {
		int numeroDeQuadros = (int) Math.ceil((double) quadroEnquadrado.length / tamanhoDoQuadro);
		int[][] quadros = new int[numeroDeQuadros][];

		for (int i = 0; i < numeroDeQuadros; i++) {
			int inicio = i * tamanhoDoQuadro;
			int fim = Math.min(inicio + tamanhoDoQuadro, quadroEnquadrado.length);
			int tamanhoAtualDoQuadro = fim - inicio;

			quadros[i] = new int[tamanhoAtualDoQuadro];
			System.arraycopy(quadroEnquadrado, inicio, quadros[i], 0, tamanhoAtualDoQuadro);
		}

		return quadros;
	} // fim do metodo

} // fim da classe
