package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import dto.DireccionDTO;

@Entity
@Table(name = "Direcciones")
public class Direccion extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int idDireccion;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String calle;

	@Column(nullable = true)
	private int numero;

	@Column(nullable = true)
	private int piso;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String departamento;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String CP;

	public Direccion() {
	}

	public Direccion(int idDireccion, String calle, int numero, int piso,
			String departamento, String CP) {
		super();
		this.idDireccion = idDireccion;
		this.calle = calle;
		this.numero = numero;
		this.piso = piso;
		this.departamento = departamento;
		this.CP = CP;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public int getPiso() {
		return piso;
	}

	public void setPiso(int piso) {
		this.piso = piso;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	public String getCP() {
		return CP;
	}

	public void setCP(String CP) {
		this.CP = CP;
	}

	public DireccionDTO toDTO() {
		DireccionDTO direccion = new DireccionDTO(idDireccion, calle, numero,
				piso, departamento, CP);
		return direccion;
	}
}
