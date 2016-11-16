package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import dto.RemitoDTO;

@Entity
@Table(name = "Remitos")
public class Remito extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(nullable = false)
	private int idRemito;

	@OneToOne
	@JoinColumn(name = "idPedido")
	private Pedido pedido;

	public Remito() {
	}

	public Remito(int idRemito, Pedido pedido) {
		super();
		this.idRemito = idRemito;
		this.pedido = pedido;
	}

	public int getIdRemito() {
		return idRemito;
	}

	public void setIdRemito(int idRemito) {
		this.idRemito = idRemito;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public RemitoDTO toDTO() {
		return new RemitoDTO(idRemito, pedido.toDTO());
	}
}
