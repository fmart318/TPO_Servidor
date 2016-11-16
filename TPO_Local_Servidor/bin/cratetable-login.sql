use master
drop database Jugadores
CREATE DATABASE Jugadores
ON PRIMARY(
	NAME = Jugadores,
	FILENAME = 'C:\Program Files\Microsoft SQL Server\MSSQL12.MSSQLSERVER\MSSQL\DATA\Jugadores_dat.mdf',
	SIZE = 5 MB,
--	MAXSIZE = ,
	FILEGROWTH = 1
)
LOG ON(
	NAME = Jugadores_log,
	FILENAME = 'C:\Program Files\Microsoft SQL Server\MSSQL12.MSSQLSERVER\MSSQL\DATA\Jugadores_log.ldf',
	SIZE = 1 MB,
	FILEGROWTH = 1
)
GO
CREATE LOGIN Jugadores
	WITH PASSWORD = 'Jugadores',
	DEFAULT_DATABASE = Jugadores,
	CHECK_EXPIRATION = OFF,
	CHECK_POLICY = OFF
GO
use Jugadores
GO	
sp_changedbowner  Jugadores
GO
