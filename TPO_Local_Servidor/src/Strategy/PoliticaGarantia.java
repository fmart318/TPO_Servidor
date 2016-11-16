package Strategy;

import dto.VehiculoDTO;

public class PoliticaGarantia implements PoliticaMantenimiento{

	public void mandarAMantenimiento(VehiculoDTO vehiculoDTO) {
		// TODO Auto-generated method stub
		vehiculoDTO.setEstado("En mantenimiento por garantia.");
	}

	
}
