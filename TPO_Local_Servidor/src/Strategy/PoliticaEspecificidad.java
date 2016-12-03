package Strategy;

import entities.Vehiculo;

public class PoliticaEspecificidad implements PoliticaMantenimiento{

	@Override
	public void mandarAMantenimiento(Vehiculo vehiculo) {
		vehiculo.setEstadoMantenimientoEspecifico();
	}

}
