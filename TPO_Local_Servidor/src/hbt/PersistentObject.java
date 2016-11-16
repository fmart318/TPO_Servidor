package hbt;

import java.io.Serializable;

import javax.persistence.*;

@MappedSuperclass
public class PersistentObject implements Serializable{
	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
}
