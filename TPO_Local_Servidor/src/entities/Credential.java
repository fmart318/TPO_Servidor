package entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Credential")
public class Credential {

	public Credential() {
		super();
	}

	public Credential(String username, String password, String type) {
		super();
		this.username = username;
		this.password = password;
		this.type = type;
	}

	@Id
	@Column(name = "username", columnDefinition = "varchar(50)", nullable = false)
	private String username;
	@Column(name = "password", columnDefinition = "varchar(50)", nullable = true)
	private String password;
	@Column(name = "type", columnDefinition = "varchar(50)", nullable = true)
	private String type;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
