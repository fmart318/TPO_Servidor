package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import dto.ProductoDTO;

@Entity
@Table(name = "Productos")
public class Producto extends PersistentObject {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "idProducto", columnDefinition = "int", nullable = false)
	private int idProducto;
	@Column(name = "nombre", columnDefinition = "varchar(50)", nullable = true)
	private String nombre;
	@Column(name = "tipo", columnDefinition = "varchar(50)", nullable = true)
	private String tipo;

	public Producto() {
	}

	public Producto(int idProducto, String nombre, String tipo) {
		super();
		this.idProducto = idProducto;
		this.nombre = nombre;
		this.tipo = tipo;
	}

	public int getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(int idProducto) {
		this.idProducto = idProducto;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public ProductoDTO toDTO() {
		return new ProductoDTO(idProducto, nombre, tipo);
	}
}
