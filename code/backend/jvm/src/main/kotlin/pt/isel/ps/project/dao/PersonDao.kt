package pt.isel.ps.project.dao

import org.jdbi.v3.core.statement.OutParameters
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.customizer.OutParameter
import org.jdbi.v3.sqlobject.statement.SqlCall
import org.jdbi.v3.sqlobject.statement.SqlQuery
import pt.isel.ps.project.model.person.*
import java.util.UUID

interface PersonDao {
    @SqlQuery("SELECT get_persons();")
    fun getPersons(): String

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL create_person(:$PERSON_REP, :role, :name, :email, :password, :phone, :company, :skill);")
    fun createPerson(@BindBean person: CreatePersonEntity): OutParameters

    @SqlQuery("SELECT get_person('4b341de0-65c0-4526-8898-24de463fc315', :reqPersonId);") // TODO: Diogo Novo | Admin
    fun getPerson(reqPersonId: UUID): String

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL update_person(:$PERSON_REP, :personId, :name, :phone, :email, :password);")
    fun updatePerson(personId: UUID, @BindBean person: UpdatePersonEntity): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL delete_user(:$PERSON_REP, :personId);")
    fun deleteUser(personId: UUID): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER) // TODO: Diogo Novo | Admin
    @SqlCall("CALL fire_person(:$PERSON_REP, '4b341de0-65c0-4526-8898-24de463fc315', :personId, :companyId, :reason);")
    fun firePerson(personId: UUID, companyId: Long, @BindBean info: FireBanPersonEntity): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER) // TODO: Diogo Novo | Admin
    @SqlCall("CALL rehire_person(:$PERSON_REP, '4b341de0-65c0-4526-8898-24de463fc315', :personId, :companyId);")
    fun rehirePerson(personId: UUID, companyId: Long): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER) // TODO: Diogo Novo | Admin
    @SqlCall("CALL ban_person(:$PERSON_REP, '4b341de0-65c0-4526-8898-24de463fc315', :personId, :reason);")
    fun banPerson(personId: UUID, @BindBean info: FireBanPersonEntity): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER) // TODO: Diogo Novo | Admin
    @SqlCall("CALL unban_person(:$PERSON_REP, '4b341de0-65c0-4526-8898-24de463fc315', :personId);")
    fun unbanPerson(personId: UUID): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL add_role_to_person(:$PERSON_REP, :personId, :role, :company, :skill);")
    fun addRoleToPerson(personId: UUID, @BindBean info: AddRoleToPersonEntity): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL remove_role_from_person(:$PERSON_REP, :personId, :role);")
    fun removeRoleFromPerson(personId: UUID, @BindBean info: RemoveRoleFromPersonEntity): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL add_skill_to_employee(:$PERSON_REP, :personId, :skill);")
    fun addSkillToEmployee(personId: UUID, @BindBean info: AddRemoveSkillToEmployeeEntity): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL remove_skill_from_employee(:$PERSON_REP, :personId, :skill);")
    fun removeSkillFromEmployee(personId: UUID, @BindBean info: AddRemoveSkillToEmployeeEntity): OutParameters

    @OutParameter(name = PERSON_REP, sqlType = java.sql.Types.OTHER)
    @SqlCall("CALL assign_person_to_company(:$PERSON_REP, :personId, :company);")
    fun assignPersonToCompany(personId: UUID, @BindBean info: AssignPersonToCompanyEntity): OutParameters
}