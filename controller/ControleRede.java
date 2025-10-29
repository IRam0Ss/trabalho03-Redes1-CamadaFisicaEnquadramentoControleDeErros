package controller;

import model.AplicacaoReceptora;
import model.AplicacaoTransmissora;
import model.CamadaAplicacaoReceptora;
import model.CamadaAplicacaoTransmissora;
import model.CamadaEnlaceDadosReceptora;
import model.CamadaEnlaceDadosTransmissora;
import model.CamadaFisicaReceptora;
import model.CamadaFisicaTransmissora;
import model.MeioDeComunicacao;

/**
 * Classe responsavel por gerenciar a comunicacao entre as camadas da rede
 */
public class ControleRede {

	// usada para aturalizar a interface grafica
	private ControlerTelaPrincipal controlerTelaPrincipal;

	// pilha de Transmissao
	private AplicacaoTransmissora aplicacaoTransmissora;
	private CamadaAplicacaoTransmissora camadaAplicacaoTransmissora;
	private CamadaEnlaceDadosTransmissora camadaEnlaceDadosTransmissora;
	private CamadaFisicaTransmissora camadaFisicaTransmissora;

	// pilha de Recepcao
	private AplicacaoReceptora aplicacaoReceptora;
	private CamadaAplicacaoReceptora camadaAplicacaoReceptora;
	private CamadaEnlaceDadosReceptora camadaEnlaceDadosReceptora;
	private CamadaFisicaReceptora camadaFisicaReceptora;

	// meio de Comunicacao Central
	private MeioDeComunicacao meioDeComunicacao;

	public ControleRede(ControlerTelaPrincipal controlerTelaPrincipal) {

		this.controlerTelaPrincipal = controlerTelaPrincipal;

		// montar a infraestrutura de rede

		// passa o controleTelaPricipal para as cmadas que precisam atualizar a
		// interface grafica

		// recepcao
		this.aplicacaoReceptora = new AplicacaoReceptora(this.controlerTelaPrincipal);
		this.camadaAplicacaoReceptora = new CamadaAplicacaoReceptora(this.aplicacaoReceptora, this.controlerTelaPrincipal);
		this.camadaEnlaceDadosReceptora = new CamadaEnlaceDadosReceptora(this.camadaAplicacaoReceptora,
				this.controlerTelaPrincipal);
		this.camadaFisicaReceptora = new CamadaFisicaReceptora(this.camadaEnlaceDadosReceptora,
				this.controlerTelaPrincipal);

		// transmissao
		this.camadaFisicaTransmissora = new CamadaFisicaTransmissora(this.controlerTelaPrincipal);
		this.camadaEnlaceDadosTransmissora = new CamadaEnlaceDadosTransmissora(this.camadaFisicaTransmissora,
				this.controlerTelaPrincipal);
		this.camadaAplicacaoTransmissora = new CamadaAplicacaoTransmissora(this.camadaEnlaceDadosTransmissora,
				this.controlerTelaPrincipal);
		this.aplicacaoTransmissora = new AplicacaoTransmissora(this.camadaAplicacaoTransmissora);

		// meio de comunicacao
		this.meioDeComunicacao = new MeioDeComunicacao(this.camadaFisicaTransmissora, this.camadaFisicaReceptora, this.controlerTelaPrincipal);

		// informa as camadas fisicas sobre o meio que elas devem usar
		this.camadaFisicaTransmissora.setMeioDeComunicacao(this.meioDeComunicacao);
		this.camadaFisicaReceptora.setMeioDeComunicacao(this.meioDeComunicacao);

	} // fim construtor

	/**
	 * metodo que inicia a simulacao de transmissao de dados na rede
	 * 
	 * @param mensagem mensagem a ser transmitida
	 */
	public void iniciarSimulacao(String mensagem) {
		this.aplicacaoTransmissora.iniciarTransmissao(mensagem);
	}// fim metodo iniciarSimulacao

} // fim classe ControleRede
