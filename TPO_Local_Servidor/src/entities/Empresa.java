package entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import dto.EmpresaDTO;
import dto.ProductoDTO;

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
	@OneToMany
	@JoinColumn(name = "idProducto")
	private List<Producto> productos;
	@Column(name = "saldoCuentaCorriente", nullable = true)
	private float saldoCuentaCorriente;

	public Empresa() {
		super();
	}

	public Empresa(int idCliente, String nombre, int CUIT, String tipo,
			String detallePoliticas, List<Producto> productos,
			float saldoCuentaCorriente) {
		super(idCliente, nombre);
		this.CUIT = CUIT;
		this.tipo = tipo;
		this.detallePoliticas = detallePoliticas;
		this.productos = productos;
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

	public List<Producto> getProductos() {
		return productos;
	}

	public void setProductos(List<Producto> productos) {
		this.productos = productos;
	}

	public float getSaldoCuentaCorriente() {
		return saldoCuentaCorriente;
	}

	public void setSaldoCuentaCorriente(float saldoCuentaCorriente) {
		this.saldoCuentaCorriente = saldoCuentaCorriente;
	}

	public EmpresaDTO toDTO() {
		List<ProductoDTO> productosDTO = new ArrayList<ProductoDTO>();
		for (Producto producto : productos)
			productosDTO.add(new ProductoDTO(producto.getIdProducto(), producto
					.getNombre(), producto.getTipo()));
		return new EmpresaDTO(idCliente, nombre, CUIT, tipo, detallePoliticas,
				productosDTO, saldoCuentaCorriente);
	}
}
