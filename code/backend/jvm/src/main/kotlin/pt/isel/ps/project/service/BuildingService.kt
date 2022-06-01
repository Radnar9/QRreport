package pt.isel.ps.project.service

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.springframework.stereotype.Service
import pt.isel.ps.project.dao.BuildingDao
import pt.isel.ps.project.exception.Errors
import pt.isel.ps.project.exception.InternalServerException
import pt.isel.ps.project.model.building.BuildingsDto
import pt.isel.ps.project.model.building.BuildingDto
import pt.isel.ps.project.model.building.CreateBuildingEntity
import pt.isel.ps.project.model.building.BuildingItemDto
import pt.isel.ps.project.model.building.BUILDING_REP
import pt.isel.ps.project.model.building.UpdateBuildingEntity
import pt.isel.ps.project.model.building.ChangeManagerEntity
import pt.isel.ps.project.model.building.BuildingManagerDto
import pt.isel.ps.project.util.Validator.Company.Building.verifyCreateBuildingInput
import pt.isel.ps.project.util.Validator.Company.Building.verifyUpdateBuildingInput
import pt.isel.ps.project.util.deserializeJsonTo

@Service
class BuildingService(jdbi: Jdbi) {

    private val buildingDao = jdbi.onDemand<BuildingDao>()

    fun getBuildings(companyId: Long): BuildingsDto {
        return buildingDao.getBuildings(companyId).deserializeJsonTo()
    }

    fun getBuilding(companyId: Long, buildingId: Long): BuildingDto {
        return buildingDao.getBuilding(companyId, buildingId).deserializeJsonTo()
    }

    fun createBuilding(companyId: Long, building: CreateBuildingEntity): BuildingItemDto {
        //verifyPersonId(manager.manager) TODO
        verifyCreateBuildingInput(building)
        return buildingDao.createBuilding(companyId, building).getString(BUILDING_REP)
            ?.deserializeJsonTo()
            ?: throw InternalServerException(Errors.InternalServerError.Message.INTERNAL_ERROR)
    }

    fun updateBuilding(companyId: Long, buildingId: Long, building: UpdateBuildingEntity): BuildingItemDto {
        verifyUpdateBuildingInput(building)
        return buildingDao.updateBuilding(companyId, buildingId, building).getString(BUILDING_REP)
            ?.deserializeJsonTo()
            ?: throw InternalServerException(Errors.InternalServerError.Message.INTERNAL_ERROR)
    }

    fun deactivateBuilding(companyId: Long, buildingId: Long): BuildingItemDto {
        return buildingDao.deactivateBuilding(companyId, buildingId).getString(BUILDING_REP)
            ?.deserializeJsonTo()
            ?: throw InternalServerException(Errors.InternalServerError.Message.INTERNAL_ERROR)
    }

    fun activateBuilding(companyId: Long, buildingId: Long): BuildingItemDto {
        return buildingDao.activateBuilding(companyId, buildingId).getString(BUILDING_REP)
            ?.deserializeJsonTo()
            ?: throw InternalServerException(Errors.InternalServerError.Message.INTERNAL_ERROR)
    }

    fun changeBuildingManager(companyId: Long, buildingId: Long, manager: ChangeManagerEntity): BuildingManagerDto {
        //verifyPersonId(manager.manager) TODO
        return buildingDao.changeBuildingManager(companyId, buildingId, manager).getString(BUILDING_REP)
            ?.deserializeJsonTo()
            ?: throw InternalServerException(Errors.InternalServerError.Message.INTERNAL_ERROR)
    }
}