package pt.isel.ps.project.responses

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import pt.isel.ps.project.auth.AuthPerson
import pt.isel.ps.project.model.Uris
import pt.isel.ps.project.model.Uris.Tickets
import pt.isel.ps.project.model.Uris.Tickets.TICKETS_PAGINATION
import pt.isel.ps.project.model.person.PersonItemDto
import pt.isel.ps.project.model.person.PersonsDto
import pt.isel.ps.project.model.representations.CollectionModel
import pt.isel.ps.project.model.representations.DEFAULT_PAGE
import pt.isel.ps.project.model.representations.QRreportJsonModel
import pt.isel.ps.project.model.ticket.*
import pt.isel.ps.project.responses.BuildingResponses.getBuildingItem
import pt.isel.ps.project.responses.CommentResponses.COMMENT_PAGE_MAX_SIZE
import pt.isel.ps.project.responses.CommentResponses.getCommentsRepresentation
import pt.isel.ps.project.responses.CompanyResponses.getCompanyItem
import pt.isel.ps.project.responses.DeviceResponses.getDeviceItem
import pt.isel.ps.project.responses.PersonResponses.getPersonItem
import pt.isel.ps.project.responses.Response.Classes
import pt.isel.ps.project.responses.Response.Links
import pt.isel.ps.project.responses.Response.Relations
import pt.isel.ps.project.responses.Response.buildResponse
import pt.isel.ps.project.responses.Response.setLocationHeader
import pt.isel.ps.project.responses.RoomResponses.getRoomItem
import pt.isel.ps.project.util.Validator.Auth.Roles.isAdmin
import pt.isel.ps.project.util.Validator.Auth.Roles.isEmployee
import pt.isel.ps.project.util.Validator.Auth.Roles.isManager
import pt.isel.ps.project.util.Validator.Auth.Roles.isUser
import pt.isel.ps.project.util.Validator.Person.belongsToCompany
import pt.isel.ps.project.util.Validator.Person.isEmployeeTicket

object TicketResponses {
    const val TICKET_PAGE_MAX_SIZE = 10

    object Actions {
        fun deleteTicket(ticketId: Long) = QRreportJsonModel.Action(
            name = "delete-ticket",
            title = "Delete ticket",
            method = HttpMethod.DELETE,
            href = Tickets.makeSpecific(ticketId)
        )

        fun updateTicket(ticketId: Long) = QRreportJsonModel.Action(
            name = "update-ticket",
            title = "Update ticket",
            method = HttpMethod.PUT,
            href = Tickets.makeSpecific(ticketId),
            type = MediaType.APPLICATION_JSON.toString(),
            properties = listOf(
                QRreportJsonModel.Property("subject", "string", required = false),
                QRreportJsonModel.Property("description", "string", required = false)
            )
        )

        fun changeTicketState(ticketId: Long) = QRreportJsonModel.Action(
            name = "update-state",
            title = "Update state",
            method = HttpMethod.PUT,
            href = Tickets.makeState((ticketId)),
            type = MediaType.APPLICATION_JSON.toString(),
            properties = listOf(
                QRreportJsonModel.Property("state", "number"),
            )
        )

        fun setEmployee(ticketId: Long) = QRreportJsonModel.Action(
            name = "set-employee",
            title = "Set employee",
            method = HttpMethod.PUT,
            href = Tickets.makeEmployee((ticketId)),
            type = MediaType.APPLICATION_JSON.toString(),
            properties = listOf(
                QRreportJsonModel.Property(name = "employee", type = "string",
                    possibleValues = QRreportJsonModel.PropertyValue(Uris.Persons.BASE_PATH)),
            )
        )

        fun removeEmployee(ticketId: Long) = QRreportJsonModel.Action(
            name = "remove-employee",
            title = "Remove employee",
            method = HttpMethod.DELETE,
            href = Tickets.makeEmployee((ticketId))
        )

        fun addRate(ticketId: Long) = QRreportJsonModel.Action(
            name = "add-rate",
            title = "Add rate",
            method = HttpMethod.PUT,
            href = Tickets.makeRate((ticketId)),
            type = MediaType.APPLICATION_JSON.toString(),
            properties = listOf(
                QRreportJsonModel.Property("rate", "number"),
            )
        )

        fun groupTicket(ticketId: Long) = QRreportJsonModel.Action(
            name = "group-ticket",
            title = "Group ticket",
            method = HttpMethod.PUT,
            href = Tickets.makeGroup((ticketId)),
            type = MediaType.APPLICATION_JSON.toString(),
            properties = listOf(
                QRreportJsonModel.Property("ticket", "number",
                    possibleValues = QRreportJsonModel.PropertyValue(Tickets.BASE_PATH)),
            )
        )
    }

    private fun getTicketItem(ticket: TicketItemDto, rel: List<String>?) = QRreportJsonModel(
        clazz = listOf(Classes.TICKET),
        rel = rel,
        properties = ticket,
        links = listOf(Links.self(Tickets.makeSpecific(ticket.id)))
    )

    fun getTicketsRepresentation(ticketsDto: TicketsDto, pageIdx: Int) = QRreportJsonModel(
        clazz = listOf(Classes.TICKET, Classes.COLLECTION),
        properties = CollectionModel(pageIdx, TICKET_PAGE_MAX_SIZE, ticketsDto.ticketsCollectionSize),
        entities = mutableListOf<QRreportJsonModel>().apply {
            if (ticketsDto.tickets != null) addAll(ticketsDto.tickets.map { getTicketItem(it, listOf(Relations.ITEM)) })
        },
        links = listOf(
            Links.self(Uris.makePagination(pageIdx, Tickets.BASE_PATH)),
            Links.pagination(TICKETS_PAGINATION),
        ),
    )

    fun getTicketRepresentation(user: AuthPerson, ticketInfo: TicketExtraInfo) = QRreportJsonModel(
        clazz = listOf(Classes.TICKET),
        properties = ticketInfo.ticket,
        entities = mutableListOf<QRreportJsonModel>().apply {
            add(getCommentsRepresentation(
                user,
                ticketInfo.ticketComments,
                ticketInfo.parentTicket ?: ticketInfo.ticket.id,
                ticketInfo.ticket.employeeState,
                CollectionModel(DEFAULT_PAGE, COMMENT_PAGE_MAX_SIZE, ticketInfo.ticketComments.collectionSize),
                ticketInfo.ticketComments.isTicketChild,
                listOf(Relations.TICKET_COMMENTS))
            )
            add(getCompanyItem(ticketInfo.company, listOf(Relations.TICKET_COMPANY)))
            add(getBuildingItem(ticketInfo.company.id, ticketInfo.building, listOf(Relations.TICKET_BUILDING)))
            add(getRoomItem(ticketInfo.room.id, ticketInfo.building.id, ticketInfo.room, listOf(Relations.TICKET_ROOM)))
            add(getDeviceItem(ticketInfo.device, listOf(Relations.TICKET_DEVICE)))
            add(getPersonItem(ticketInfo.person, listOf(Relations.TICKET_AUTHOR)))
            add(getParentTicket(ticketInfo.parentTicket ?: ticketInfo.ticket.id))
        },
        actions = mutableListOf<QRreportJsonModel.Action>().apply {
            if (isUser(user) && ticketInfo.ticket.employeeState.compareTo("Archived") == 0)
                add(Actions.addRate(ticketInfo.ticket.id))
            if (ticketInfo.parentTicket != null) return@apply
            if (ticketInfo.ticket.employeeState.compareTo("Refused") == 0 ||
                ticketInfo.ticket.employeeState.compareTo("Archived") == 0) return@apply
            if (ticketInfo.ticket.employeeState.compareTo("To assign") == 0)
                add(Actions.updateTicket(ticketInfo.ticket.id))
            if (ticketInfo.employee != null && (isEmployee(user) && isEmployeeTicket(user, ticketInfo.employee.id)) ||
                (isManager(user) && belongsToCompany(user, ticketInfo.company.id)) ||
                isAdmin(user)
            )
                add(Actions.changeTicketState(ticketInfo.ticket.id))
            if (isManager(user) && belongsToCompany(user, ticketInfo.company.id) || isAdmin(user)) {
                if (ticketInfo.employee == null && ticketInfo.ticket.employeeState.compareTo("Refused") != 0
                    && ticketInfo.ticket.employeeState.compareTo("Completed") != 0) {
                    add(Actions.groupTicket(ticketInfo.ticket.id))
                    add(Actions.setEmployee(ticketInfo.ticket.id))
                }
                else add(Actions.removeEmployee(ticketInfo.ticket.id))
            }
        },
        links = listOf(Links.self(Tickets.makeSpecific(ticketInfo.ticket.id)), Links.tickets())
    )

    fun createTicketRepresentation(ticket: TicketItemDto) = buildResponse(
        QRreportJsonModel(
            clazz = listOf(Classes.TICKET),
            properties = ticket,
            links = listOf(Links.self(Tickets.makeSpecific(ticket.id)))
        ),
        HttpStatus.CREATED,
        setLocationHeader(Tickets.makeSpecific(ticket.id)),
    )

    fun updateTicketRepresentation(ticket: TicketItemDto) = buildResponse(
        QRreportJsonModel(
            clazz = listOf(Classes.TICKET),
            properties = ticket,
            links = listOf(Links.self(Tickets.makeSpecific(ticket.id)))
        )
    )

    fun addTicketRateRepresentation(ticket: TicketRate) = buildResponse(
        QRreportJsonModel(
            clazz = listOf(Classes.TICKET),
            properties = ticket,
            links = listOf(Links.self(Tickets.makeSpecific(ticket.id)))
        )
    )

    fun changeTicketStateRepresentation(ticket: TicketItemDto) = buildResponse(
        QRreportJsonModel(
            clazz = listOf(Classes.TICKET),
            properties = ticket,
            links = listOf(Links.self(Tickets.makeSpecific(ticket.id)))
        )
    )

    fun getSpecificEmployeesRepresentation(personsDto: PersonsDto, pageIdx: Int) = buildResponse(
        QRreportJsonModel(
            clazz = listOf(Classes.PERSON, Classes.COLLECTION),
            properties = CollectionModel(pageIdx, PersonResponses.PERSON_PAGE_MAX_SIZE, personsDto.personsCollectionSize),
            entities = mutableListOf<QRreportJsonModel>().apply {
                if (personsDto.persons != null)
                    addAll(personsDto.persons.map {
                        getPersonItem(it, listOf(Relations.ITEM))
                    })
            },
            links = listOf(
                Links.self(Uris.makePagination(pageIdx, Uris.Persons.BASE_PATH)),
                Links.pagination(Uris.Persons.PERSONS_PAGINATION),
            ),
        )
    )

    fun setEmployeeRepresentation(ticket: TicketEmployee) = buildResponse(
        QRreportJsonModel(
            clazz = listOf(Classes.TICKET),
            properties = ticket.ticket,
            entities = listOf(getPersonItem(ticket.person, listOf(Relations.TICKET_EMPLOYEE))),
            links = listOf(Links.self(Tickets.makeSpecific(ticket.ticket.id)))
        )
    )

    fun removeEmployeeRepresentation(ticket: TicketEmployee) = QRreportJsonModel(
        clazz = listOf(Classes.TICKET),
        properties = ticket.ticket,
        entities = listOf(getPersonItem(ticket.person, listOf(Relations.TICKET_EMPLOYEE))),
        links = listOf(Links.self(Tickets.makeSpecific(ticket.ticket.id)))
    )

    fun groupTicketRepresentation(ticket: TicketItemDto) = QRreportJsonModel(
        clazz = listOf(Classes.TICKET),
        properties = ticket,
        links = listOf(Links.self(Tickets.makeSpecific(ticket.id)))
    )

    private fun getParentTicket(ticketId: Long) = QRreportJsonModel(
        clazz = listOf(Classes.PERSON),
        rel = listOf(Relations.PARENT_TICKET),
        href = Tickets.makeSpecific(ticketId),
    )
}