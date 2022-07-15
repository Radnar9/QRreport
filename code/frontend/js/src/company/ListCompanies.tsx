import { useMemo, useState } from "react"
import { Link } from "react-router-dom"
import { Loading } from "../components/Various"
import { DisplayError } from "../Error"
import { useFetch } from "../hooks/useFetch"
import { Company } from "../models/Models"
import { Action, Entity } from "../models/QRJsonModel"
import { Collection } from "../pagination/CollectionPagination"
import { COMPANIES_URL_API } from "../Urls"
import { getEntitiesOrUndefined, getActionsOrUndefined } from "../models/ModelUtils"
import { InsertCompany } from "./InsertCompany"
import { ActionComponent } from "../user/profile/ActionRequest"

export function ListCompanies() {

    const initValues: RequestInit = {
        credentials: 'include',
        headers: { 'Request-Origin': 'WebApp' }
    }
    
    const init = useMemo(() => initValues ,[])
    const [action, setAction] = useState<Action | undefined>(undefined)
    const [payload, setPayload] = useState('')

    const { isFetching, isCanceled, cancel, result, error } = useFetch<Collection>(COMPANIES_URL_API(), init)
    
    switch (action?.name) {
        case 'create-company': return <ActionComponent action={action} extraInfo={payload} returnComponent={<ListCompanies/>} />
    }

    if (isFetching) return <Loading/>
    if (isCanceled) return <p>Canceled</p>
    if (error !== undefined) return <DisplayError error={error}/>
    
    function CompanyItemComponent({entity}: {entity: Entity<Company>}) {
        const company = entity.properties

        const bgColor = company.state === 'active' ? 'bg-white' : 'bg-red-100'
        
        return (
            <div>
                <Link to={`/companies/${company.id}`}>
                    <div className={`p-5 ${bgColor} rounded-lg border border-gray-200 shadow-md hover:bg-gray-200 divide-y space-y-4`}>  
                        <div>
                            <h5 className='mb-2 text-xl tracking-tight text-gray-900'>{company.name}</h5>
                            <p>Number of spaces: {company.numberOfBuildings}</p>
                        </div>
                    </div>
                </Link>
            </div>
        )
    }

    function CompaniesActions({actions}: {actions: Action[] | undefined}) {

        const [auxAction, setAuxAction] = useState<Action | undefined>(undefined)

        let componentsActions = actions?.map(action => {
            switch(action.name) {
                case 'create-company': return (
                        <button onClick={() => setAuxAction(action)} className="text-white bg-blue-700 hover:bg-blue-800 rounded-lg px-2">
                            {action.title}
                        </button>
                    )
            }
        })

        return (
            <>
                <div className="flex space-x-2">{componentsActions} </div>
                {auxAction?.name === 'create-company' && 
                <InsertCompany action={auxAction} setAction={setAction} setAuxAction={setAuxAction} setPayload={setPayload}/>}
            </>
        )
    }

    function Companies({ entities }: { entities?: Entity<Company>[]}) {
        if (!entities) return null
        
        return (
            <div className="space-y-3">
                {entities.map((entity, idx) => {
                    if (entity.class.includes('company') && entity.rel?.includes('item')) {
                        return <CompanyItemComponent key={idx} entity={entity}/>
                    }
                })}
            </div>
        )
    }

    return (
        <div className='px-3 pt-3 space-y-4'>
            <h1 className='text-3xl mt-0 mb-2 text-blue-800'>Companies</h1>
            {/*<Filters/>*/}
            <CompaniesActions actions={getActionsOrUndefined(result?.body)}/>
            <Companies entities={getEntitiesOrUndefined(result?.body)}/>
        </div>
    )
}