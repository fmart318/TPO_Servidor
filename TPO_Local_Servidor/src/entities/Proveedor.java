package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import dto.ProveedorDTO;

@Entity
@Table(name = "Proveedores")
@Inheritance(strategy = InheritanceType.JOINED)
public class Proveedor extends PersistentObject {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue
	@Column(name = "idProveedor", columnDefinition = "int", nullable = false)
	protected int idProveedor;
	@Column(name = "compania", columnDefinition = "varchar(50)", nullable = true)
	protected String compania;
	@Column(name = "tipoMercaderia", columnDefinition = "varchar(50)", nullable = true)
	protected String tipoMercaderia;

	public Proveedor() {
		super();
	}

	public Proveedor(int idProveedor, String compania, String tipoMercaderia) {
		super();
		this.idProveedor = idProveedor;
		this.compania = compania;
		this.tipoMercaderia = tipoMercaderia;
	}

	public String getTipoMercaderia() {
		return tipoMercaderia;
	}

	public void setTipoMercaderia(String tipoMercaderia) {
		this.tipoMercaderia = tipoMercaderia;
	}

	public int getIdProveedor() {
		return idProveedor;
	}

	public void setIdProveedor(int idProveedor) {
		this.idProveedor = idProveedor;
	}

	public String getCompania() {
		return compania;
	}

	public void setCompania(String compania) {
		this.compania = compania;
	}

	public ProveedorDTO toDTO() {
		return new ProveedorDTO(idProveedor, compania, tipoMercaderia);
	}
}
