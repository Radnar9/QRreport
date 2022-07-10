import { useForm } from "react-hook-form";
import { Navigate } from "react-router-dom";
import { CreateButton, Form, Header, HeaderParagraph, Input, InputProps, Paragraph, SubmitButton } from "../../components/FormComponents";
import { useLoggedInState } from "../Session.tsx";

export default function LoginForm() {

    type loginData = { 
        email: string,
        password: string
    }

    const { register, handleSubmit, formState: { errors }} = useForm<loginData>();

    const userSession = useLoggedInState()

    const onSubmitHandler = handleSubmit(({ email, password }) => {
        console.log(email, password);
        if (email && password && userSession) {
            userSession.login(email, password);
        }
    })
    
    const emailInput: InputProps = {
        inputLabelName: 'Email *',
        register: register("email", {required: 'Is required', pattern: /.+@.+/, minLength: 4, maxLength: 320}),
        style: {borderColor: errors.email ? 'red': 'black'},
        name: "email",
        type: "email",
        errorMessage: errors.email && 'Invalid email'
    }

    const passwordInput: InputProps = {
        inputLabelName: 'Password *',
        register: register("password", {required: 'Is required', minLength: 1, maxLength: 127}),
        style: {borderColor: errors.password ? 'red': 'black'},
        name: "password",
        type: "password",
        errorMessage: errors.email && 'Invalid password'
    }

    //email and password
    return useLoggedInState()?.isLoggedIn ? <Navigate to={'/'}/> : (
        <section className="info-section">
            <div className="space-y-3 grid place-items-center"> 
                <Form onSubmitHandler = { onSubmitHandler }>
                    <Header heading='Great to see you again'>
                        <HeaderParagraph paragraph='Insert your credentials below'/>
                    </Header>
                    <Input value = {emailInput}/>
                    <Input value = {passwordInput}/>
                    <SubmitButton text={'Login'}/>
                    <CreateButton text={'Signup'} redirectUrl={'/signup'}/>
                    <Paragraph value = {'(*) Required'}/>
                </Form>
            </div>
        </section>
    )
}