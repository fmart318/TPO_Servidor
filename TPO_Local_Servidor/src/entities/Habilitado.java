package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import dto.HabilitadoDTO;

@Entity
@Table(name = "Habilitados")
public class Habilitado extends PersistentObject {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String dniHabilitado;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String nombre;

	public Habilitado(String dniHabilitado, String nombre) {
		super();
		this.dniHabilitado = dniHabilitado;
		this.nombre = nombre;
	}

	public Habilitado() {
		super();
	}

	public String getDniHabilitado() {
		return dniHabilitado;
	}

	public void setDniHabilitado(String dniHabilitado) {
		this.dniHabilitado = dniHabilitado;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public HabilitadoDTO toDTO() {
		return new HabilitadoDTO(dniHabilitado, nombre);
	}

	/*
	 * List<HabilitadoDTO> habilitadosDTO = new ArrayList<HabilitadoDTO>();
	 * for(Habilitado habilitado : habilitados)
	 * habilitadosDTO.add(habilitado.toDTO());
	 */
}
