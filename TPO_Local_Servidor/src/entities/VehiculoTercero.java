package entities;

import hbt.PersistentObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import dto.PedidoDTO;
import dto.VehiculoTerceroDTO;

@Entity
@Table(name = "VehiculosTerceros")
public class VehiculoTercero extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "idVehiculoTercero", columnDefinition = "int", nullable = false)
	private int idVehiculoTercero;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String tipoVehiculo;

	@Column(name = "precio", nullable = true)
	private float precio;
	
	@Column(name = "estado", nullable = true)
	private String estado;
	
	@Column(columnDefinition = "datetime", nullable = true)
	private Date fechaLlegada;

	@OneToMany
	@JoinColumn(name = "idVehiculoTercero", nullable = true, updatable = true, insertable = true)
	private List<Pedido> pedidos;

	public VehiculoTercero() {
		super();
	}

	public VehiculoTercero(int idVehiculoTercero, String tipoVehiculo,
			float precio, String estado, Date fechaLlegada, List<Pedido> pedidos) {
		super();
		this.idVehiculoTercero = idVehiculoTercero;
		this.tipoVehiculo = tipoVehiculo;
		this.precio = precio;
	}

	public int getIdVehiculoTercero() {
		return idVehiculoTercero;
	}

	public void setIdVehiculoTercero(int idVehiculoTercero) {
		this.idVehiculoTercero = idVehiculoTercero;
	}

	public String getTipoVehiculo() {
		return tipoVehiculo;
	}

	public void setTipoVehiculo(String tipoVehiculo) {
		this.tipoVehiculo = tipoVehiculo;
	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public List<Pedido> getPedidos() {
		return pedidos;
	}

	public void setPedidos(List<Pedido> pedidos) {
		this.pedidos = pedidos;
	}
	
	public Date getFechaLlegada() {
		return fechaLlegada;
	}

	public void setFechaLlegada(Date fechaLlegada) {
		this.fechaLlegada = fechaLlegada;
	}
	
	public VehiculoTerceroDTO toDTO() {
		List<PedidoDTO> pedidoDtos = new ArrayList<PedidoDTO>();
		for (Pedido pedido : this.pedidos) {
			pedidoDtos.add(pedido.toDTO());
		}
		return new VehiculoTerceroDTO(idVehiculoTercero, tipoVehiculo, precio, estado, fechaLlegada, pedidoDtos);
	}

}
