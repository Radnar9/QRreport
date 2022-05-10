package pt.isel.ps.project.exception

import org.springframework.http.HttpStatus
import java.net.URI

object Errors {
    object NotFound {
        val TYPE = URI("/errors/not-found")
        val STATUS = HttpStatus.NOT_FOUND

        object Message {
            const val RESOURCE_NOT_FOUND = "The resource was not found."
        }
    }

    object BadRequest {
        val TYPE = URI("/errors/validation-error")
        val STATUS = HttpStatus.BAD_REQUEST

        object Locations {
            const val PATH = "path"
            const val HEADERS = "headers"
            const val QUERY_STRING = "query_string"
            const val BODY = "body"
        }

        object Message {
            const val INVALID_REQ_PARAMS = "One or more request parameters are not valid."
            const val MISSING_MISMATCH_REQ_BODY = "One or more request body parameters are missing or have a type mismatch."

            const val UPDATE_NULL_PARAMS = "All updatable parameters can't be null."
            const val UPDATE_NULL_PARAMS_DETAIL = " Please insert one of the possible parameters in order to update."

            object Templated {
                const val MUST_HAVE_TYPE = "The value must be of the {} type."
            }

            object Pagination {
                const val PAGE_TYPE_MISMATCH = "The value must be an integer >= 0."
            }

            object Company {
                const val INVALID_NAME_LENGTH = "The name can have a maximum of 50 characters."
            }
        }
    }

    object InternalServerError {
        val TYPE = URI("/errors/internal-server-error")
        val STATUS = HttpStatus.INTERNAL_SERVER_ERROR

        object Message {
            const val INTERNAL_ERROR = "An internal server error occurred, please try again later."
            const val DB_CREATION_ERROR = "It was not possible to create the requested resource, please try again later."
            const val UNKNOWN_ERROR = "An error occurred, please verify if your passing the right values in the request."
        }
    }

    object MethodNotAllowed {
        val TYPE = URI("/errors/method-not-allowed")
        val STATUS = HttpStatus.METHOD_NOT_ALLOWED

        object Message {
            const val METHOD_NOT_ALLOWED = "The request method is not supported for the requested instance."
        }
    }

    fun makeMessage(message: String, value: Any): String {
        return message.replace("{}", value.toString())
    }
}