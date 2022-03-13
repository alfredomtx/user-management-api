
import { Link } from "react-router-dom";

export const NotFound = () => {
    return (
        <div className="p-5">
            <h1>Page not found.</h1>

            <br/>
            <Link className="p-2 border bg-red-200" to="/">Back to Home</Link>

        </div>

    )
    
}