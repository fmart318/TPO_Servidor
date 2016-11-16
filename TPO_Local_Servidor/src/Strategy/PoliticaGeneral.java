package Strategy;

import dto.VehiculoDTO;

public class PoliticaGeneral implements PoliticaMantenimiento{

	public void mandarAMantenimiento(VehiculoDTO vehiculoDTO) {
		vehiculoDTO.setEstado("En mantenimiento general.");
		
	}
}
