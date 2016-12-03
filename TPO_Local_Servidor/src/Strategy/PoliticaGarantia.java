package Strategy;

import entities.Vehiculo;

public class PoliticaGarantia implements PoliticaMantenimiento{

	@Override
	public void mandarAMantenimiento(Vehiculo vehiculo) {
		vehiculo.setEstadoMantenimientoGarantia();
	}

	
}
