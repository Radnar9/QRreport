package utils

import pt.isel.ps.project.auth.AuthPerson
import pt.isel.ps.project.model.building.BuildingDto
import pt.isel.ps.project.model.building.BuildingItemDto
import pt.isel.ps.project.model.building.BuildingsDto
import pt.isel.ps.project.model.category.CategoriesDto
import pt.isel.ps.project.model.category.CategoryDto
import pt.isel.ps.project.model.category.CategoryItemDto
import pt.isel.ps.project.model.comment.CommentDto
import pt.isel.ps.project.model.comment.CommentItemDto
import pt.isel.ps.project.model.comment.CommentsDto
import pt.isel.ps.project.model.company.CompaniesDto
import pt.isel.ps.project.model.company.CompanyDto
import pt.isel.ps.project.model.company.CompanyItemDto
import pt.isel.ps.project.model.device.DeviceItemDto
import pt.isel.ps.project.model.device.DevicesDto
import pt.isel.ps.project.model.room.RoomDeviceDto
import pt.isel.ps.project.model.room.RoomDto
import pt.isel.ps.project.model.room.RoomItemDto
import pt.isel.ps.project.model.room.RoomsDto
import java.io.File

object Utils {
    object LoadScript {
        private val classLoader = javaClass.classLoader
        fun getResourceFile(resourceName: String) =
            File(classLoader.getResource(resourceName)!!.toURI()).readText()
    }
}

fun AuthPerson.ignoreTimestamp() = AuthPerson(id, name, phone, email, activeRole, skills, companies, null, state, reason)
fun BuildingItemDto.ignoreTimestamp() = BuildingItemDto(id, name, floors, state, null)
fun CompanyDto.ignoreTimestamp(): CompanyDto {
    val buildings = buildings?.map { it.ignoreTimestamp() }
    return CompanyDto(id, name, state, null, buildings, buildingsCollectionSize)
}
fun CompanyItemDto.ignoreTimestamp() = CompanyItemDto(id, name, state, null)
fun CompaniesDto.ignoreTimestamps() = CompaniesDto(
    companies?.map { it.ignoreTimestamp() },
    companiesCollectionSize
)

fun BuildingsDto.ignoreTimestamps() = BuildingsDto(
    buildings?.map { it.ignoreTimestamp() },
    buildingsCollectionSize
)
fun BuildingDto.ignoreTimestamp() = BuildingDto(building.ignoreTimestamp(), rooms.ignoreTimestamps(), manager)

fun RoomItemDto.ignoreTimestamp() = RoomItemDto(id, name, floor, state, null)
fun RoomsDto.ignoreTimestamps() = RoomsDto(
    rooms?.map { it.ignoreTimestamp() },
    roomsCollectionSize
)
fun RoomDto.ignoreTimestamp() = RoomDto(room.ignoreTimestamp(), devices.ignoreTimestamps())
fun RoomDeviceDto.ignoreTimestamp() = RoomDeviceDto(room.ignoreTimestamp(), device.ignoreTimestamp())

fun DeviceItemDto.ignoreTimestamp() = DeviceItemDto(id, name, category, state, null)
fun DevicesDto.ignoreTimestamps() = DevicesDto(
    devices?.map { it.ignoreTimestamp() },
    devicesCollectionSize
)

fun CategoryDto.ignoreTimestamp() = CategoryDto(id, name, state, null)
fun CategoryItemDto.ignoreTimestamp() = CategoryItemDto(category.ignoreTimestamp(), inUse)
fun CategoriesDto.ignoreTimestamps() = CategoriesDto(
    categories?.map { it.ignoreTimestamp() },
    categoriesCollectionSize,
)

fun CommentItemDto.ignoreTimestamp() = CommentItemDto(id, comment, null)
fun CommentDto.ignoreTimestamp() = CommentDto(comment.ignoreTimestamp(), person)
fun CommentsDto.ignoreTimestamps() = CommentsDto(
    comments?.map { it.ignoreTimestamp() },
    collectionSize, ticketState, isTicketChild
)