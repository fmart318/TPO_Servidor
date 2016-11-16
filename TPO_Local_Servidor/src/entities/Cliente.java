package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import dto.ClienteDTO;

@Entity
@Table(name = "Clientes")
@Inheritance(strategy = InheritanceType.JOINED)
public class Cliente extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "idCliente", columnDefinition = "int", nullable = false)
	protected int idCliente;

	@Column(name = "nombre", columnDefinition = "varchar(50)", nullable = true)
	protected String nombre;

	public Cliente() {
	}

	public Cliente(int idCliente, String nombre) {
		super();
		this.idCliente = idCliente;
		this.nombre = nombre;
	}

	public int getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(int idCliente) {
		this.idCliente = idCliente;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	

	public ClienteDTO toDTO() {
		return new ClienteDTO(idCliente, nombre);
	}
}
