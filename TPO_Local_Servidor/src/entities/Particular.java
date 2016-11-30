package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import dto.ParticularDTO;

@Entity
@Table(name = "Particulares")
public class Particular extends Cliente {

	private static final long serialVersionUID = 1L;

	@Column(columnDefinition = "int", nullable = true)
	private int DNI;

	@Column(columnDefinition = "varchar(50)", nullable = true)
	private String apellido;

	public Particular(int idCliente, String nombre, int DNI, String apellido) {
		super(idCliente, nombre);
		this.DNI = DNI;
		this.apellido = apellido;
	}

	public Particular() {
		super();
	}

	public int getDNI() {
		return DNI;
	}

	public void setDNI(int dNI) {
		DNI = dNI;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public ParticularDTO toDTO() {
		return new ParticularDTO(idCliente, nombre, DNI, apellido);
	}
}
