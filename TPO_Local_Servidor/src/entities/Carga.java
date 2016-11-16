package entities;

import hbt.PersistentObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import dto.CargaDTO;

@Entity
@Table (name = "Cargas")
public class Carga extends PersistentObject{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column (nullable = false)
	private int idCarga;
	
	@Column (nullable = true)
	private float peso;
	
	@Column (nullable = true)
	private float ancho;
	
	@Column (nullable = true)
	private float alto;
	
	@Column (nullable = true)
	private float profundidad;
	
	@Column (nullable = true)
	private float volumen;
	
	@Column (columnDefinition = "varchar(50)", nullable = true)
	private String fragilidad;
	
	@Column (columnDefinition = "varchar(50)", nullable = true)
	private String tratamiento;
	
	@Column (nullable = true)
	private int apilable;
	
	@Column (columnDefinition = "bit", nullable = true)
	private boolean refrigerable;
	
	@Column (columnDefinition = "varchar(50)", nullable = true)
	private String condiciones;
	
	@Column (columnDefinition = "bit", nullable = true)
	private boolean despachado;
	
	@Column (columnDefinition = "varchar(50)", nullable = true)
	private String tipoMercaderia;
	
	public Carga() {
		
	}
	public Carga(int idCarga,float peso, float ancho, float alto, float profundidad,
			float volumen, String fragilidad, String tratamiento, int apilable,
			boolean refrigerable, String condiciones, boolean despachado,
			String tipoMercaderia) {
		super();
		this.idCarga=idCarga;
		this.peso = peso;
		this.ancho = ancho;
		this.alto = alto;
		this.profundidad = profundidad;
		this.volumen = volumen;
		this.fragilidad = fragilidad;
		this.tratamiento = tratamiento;
		this.apilable = apilable;
		this.refrigerable = refrigerable;
		this.condiciones = condiciones;
		this.despachado = despachado;
		this.tipoMercaderia = tipoMercaderia;
	}

	public int getIdCarga() {
		return idCarga;
	}

	public void setIdCarga(int idCarga) {
		this.idCarga = idCarga;
	}

	public float getPeso() {
		return peso;
	}

	public void setPeso(float peso) {
		this.peso = peso;
	}

	public float getAncho() {
		return ancho;
	}

	public void setAncho(float ancho) {
		this.ancho = ancho;
	}

	public float getAlto() {
		return alto;
	}

	public void setAlto(float alto) {
		this.alto = alto;
	}

	public float getProfundidad() {
		return profundidad;
	}

	public void setProfundidad(float profundidad) {
		this.profundidad = profundidad;
	}

	public float getVolumen() {
		return volumen;
	}

	public void setVolumen(float volumen) {
		this.volumen = volumen;
	}

	public String getFragilidad() {
		return fragilidad;
	}

	public void setFragilidad(String fragilidad) {
		this.fragilidad = fragilidad;
	}

	public String getTratamiento() {
		return tratamiento;
	}

	public void setTratamiento(String tratamiento) {
		this.tratamiento = tratamiento;
	}

	public int getApilable() {
		return apilable;
	}

	public void setApilable(int apilable) {
		this.apilable = apilable;
	}

	public boolean isRefrigerable() {
		return refrigerable;
	}

	public void setRefrigerable(boolean refrigerable) {
		this.refrigerable = refrigerable;
	}

	public String getCondiciones() {
		return condiciones;
	}

	public void setCondiciones(String condiciones) {
		this.condiciones = condiciones;
	}

	public boolean isDespachado() {
		return despachado;
	}

	public void setDespachado(boolean despachado) {
		this.despachado = despachado;
	}

	public String getTipoMercaderia() {
		return tipoMercaderia;
	}

	public void setMercaderia(String tipoMercaderia) {
		this.tipoMercaderia = tipoMercaderia;
	}

	public CargaDTO toDTO(){
	return new CargaDTO(idCarga,peso, ancho, alto, profundidad, volumen, fragilidad, tratamiento, apilable,
			refrigerable, condiciones, despachado, tipoMercaderia);	
	}	
}
