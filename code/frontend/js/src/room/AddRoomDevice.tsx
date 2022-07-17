import { useState, useMemo, useEffect } from "react"
import { AiFillCloseCircle } from "react-icons/ai"
import { MdFilterList } from "react-icons/md"
import { TbArrowBigTop, TbArrowBigDown } from "react-icons/tb"
import { Outlet } from "react-router-dom"
import { Loading } from "../components/Various"
import { ErrorView } from "../errors/Error"
import { useFetch } from "../hooks/useFetch"
import { Device } from "../models/Models"
import { getProblemOrUndefined, getEntityOrUndefined, getLink, getPropertiesOrUndefined } from "../models/ModelUtils"
import { Action, Entity } from "../models/QRJsonModel"
import { Collection, CollectionPagination } from "../pagination/CollectionPagination"
import { BASE_URL_API } from "../Urls"

export function AddRoomDevice({action, setAction, setAuxAction, setPayload}: {
    action: Action,
    setAuxAction: React.Dispatch<React.SetStateAction<Action | undefined>>,
    setAction: React.Dispatch<React.SetStateAction<Action | undefined>> | undefined,
    setPayload: React.Dispatch<React.SetStateAction<string>>
}) {

    const initValues: RequestInit = {
        credentials: 'include',
        headers: { 'Request-Origin': 'WebApp' }
    }

    const property = action.properties.find(prop => {if(prop.name === 'device'){ return prop}})
    const href = property?.possibleValues?.href
    const url = href === undefined || null ? '' : BASE_URL_API + href

    const [direction, setDirection] = useState('desc')
    const [sortBy, setSortBy] = useState('date')
    const [device, setDevice] = useState<Device>()
    const init = useMemo(() => initValues ,[])
    const [currentUrl, setCurrentUrl] = useState(url)

    const { isFetching, result, error } = useFetch<Collection>(currentUrl, init)

    if (!action || !setPayload || !setAction) return null

    if (isFetching) return <Loading/>
    if (error) return <ErrorView/>

    const problem = getProblemOrUndefined(result?.body)
    if (problem) return <ErrorView problemJson={problem}/>
    
    function Filters() {

        const [directionAux, setDirectionAux] = useState(direction)
        const [sortByAux, setSortByAux] = useState(sortBy)

        return (
            <div className='flex w-full gap-4'>
                <select className='border rounded-lg' onChange={value => setSortByAux(value.target.value)}>
                    <option value='date'>Date</option>
                    <option value='name'>Name</option>
                </select>       
                <button 
                    className='bg-blue-800 hover:bg-blue-900 text-white font-bold rounded-lg text-sm px-5 h-12 inline-flex items-center'
                    onClick= {() => { setDirectionAux(directionAux === 'desc' ? 'asc' : 'desc') }}>
                    {directionAux === 'asc' && <TbArrowBigTop style= {{ color: 'white', fontSize: '2em' }} />}
                    {directionAux === 'desc' && <TbArrowBigDown style= {{ color: 'white', fontSize: '2em' }} />}
                </button>     
                <button className='bg-blue-800 hover:bg-blue-900 text-white font-bold rounded-lg text-sm px-5 h-12 inline-flex items-center'
                        onClick= {() => {setDirection(directionAux); setSortBy(sortByAux) }}>
                    <MdFilterList style= {{ color: 'white', fontSize: '2em' }} /> 
                </button>
            </div>    
        )
    }

    function DeviceItem({entity}: {entity: Entity<Device>}) {
        if (!entity) return null;
        const device = entity.properties
        
        return (
            <div className='flex p-5 bg-white rounded-lg border border-gray-200 shadow-md'>  
                <div className='w-full flex space-x-4'>
                    <h5 className='font-md text-gray-900'>{device.name}</h5>
                </div>
                <div className='w-full flex justify-end' >
                    <button 
                        className='px-3 py-2 text-xs font-medium text-center text-white bg-blue-700 rounded-lg hover:bg-blue-800'
                        onClick= {() => setDevice(device)}>
                        Select
                    </button>
                </div>

            </div>
        )
    }

    function Devices({entity}: { entity: Entity<Collection> | undefined }) {

        if(!entity) return null;

        return (
            <div className='flex flex-col space-y-3'>
                {entity.entities!!.map((device, idx) => <DeviceItem key={idx} entity={device}/>)}
            </div>
        )
    }

    return (
        <div className="space-y-3 p-5 bg-green rounded-lg border border-gray-200 shadow-md">
            <button onClick={() => setAuxAction(undefined)}>
                <AiFillCloseCircle style= {{ color: '#db2a0a', fontSize: "1.4em" }}/>
            </button>
            <Filters/>
            <p>Device selected: {device === undefined ? '-----' : device?.name}</p>
            <Devices entity={getEntityOrUndefined(result?.body)}/>
            <CollectionPagination collection={getPropertiesOrUndefined(result?.body)} setUrlFunction={setCurrentUrl} 
                templateUrl={getLink('pagination', result?.body)}/>
            <Outlet/>
            <div className='flex space-x-4'>
                <button className='w-full bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded'
                    onClick= {() => {setAction(action); setPayload(JSON.stringify({deviceId: device?.id}))}}>
                    {action.title}
                </button>
            </div>
        </div>
    )
}
