package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import dto.EmpresaDTO;

@Entity
@Table(name = "Empresas")
public class Empresa extends Cliente {

	private static final long serialVersionUID = 1L;

	@Column(name = "CUIT", columnDefinition = "int", nullable = true)
	private int CUIT;

	@Column(name = "tipo", columnDefinition = "varchar(50)", nullable = true)
	private String tipo;

	@Column(name = "detallePoliticas", columnDefinition = "varchar(50)", nullable = true)
	private String detallePoliticas;

	@Column(name = "saldoCuentaCorriente", nullable = true)
	private float saldoCuentaCorriente;

	public Empresa() {
		super();
	}

	public Empresa(int idCliente, String nombre, int CUIT, String tipo, String detallePoliticas,
			float saldoCuentaCorriente) {
		super(idCliente, nombre);
		this.CUIT = CUIT;
		this.tipo = tipo;
		this.detallePoliticas = detallePoliticas;
		this.saldoCuentaCorriente = saldoCuentaCorriente;
	}

	public int getCUIT() {
		return CUIT;
	}

	public void setCUIT(int CUIT) {
		this.CUIT = CUIT;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getDetallePoliticas() {
		return detallePoliticas;
	}

	public void setDetallePoliticas(String detallePoliticas) {
		this.detallePoliticas = detallePoliticas;
	}

	public float getSaldoCuentaCorriente() {
		return saldoCuentaCorriente;
	}

	public void setSaldoCuentaCorriente(float saldoCuentaCorriente) {
		this.saldoCuentaCorriente = saldoCuentaCorriente;
	}

	public EmpresaDTO toDTO() {
		return new EmpresaDTO(idCliente, nombre, CUIT, tipo, detallePoliticas, saldoCuentaCorriente);
	}
}
