package Strategy;

import dto.VehiculoDTO;
import entities.Vehiculo;

public class PoliticaGeneral implements PoliticaMantenimiento {

	@Override
	public void mandarAMantenimiento(Vehiculo vehiculo) {
		vehiculo.setEstadoMantenimientoGeneral();
	}
}
