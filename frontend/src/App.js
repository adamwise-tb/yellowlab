import { useState } from "react"; // We use this to store the "state" of the app, dependent on what buttons are clicked
import './index.css';

export default function App() {
    const [rows, setRows] = useState([]); // initialize state values to empty arrays
    const [loading, setLoading] = useState(false); // initialize state values to false

    // We'll use this function to ping the API we built with Java
    const apiRequest = async (path) => {
        setLoading(true); // This way we can create a spinning bar or something

        try {
            const req = await fetch(`/${path}`); // await bc remember: the API won't respond instantly!
            const data = await req.json();
            setRows(Array.isArray(data) ? data : []); // Once the data returns, IF rows exist, set them as the data, otherwise default to an empty array
        } finally {
            // Once complete, set loading to false, so we can remove the loading bar
            setLoading(false);
        }
    }

    // Dynamically determine how many columns are needed dependent on the data returned
    const cols = rows[0] ? Object.keys(rows[0]) : [] // if the first row exist, grab the keys as columns

    return (
        <div class='container'>
            <h1>Adam Backend Code Challenge</h1>

            <div>
                <button onClick={() => apiRequest("items")}>Items</button>
                <button onClick={() => apiRequest("distributors")}>Distributors</button>
            </div>

            {
                /* If loading is true, then display Loading... */
                loading && <p>Loading...</p>
            }

            {
               /* If we're no longer loading and we have rows present, display them in a table */
               !loading && rows.length > 0 && (
                    <div class='grid'>
                       <table>
                           <thead>
                               <tr>
                                   {
                                   /* Go through each column, and output its value */
                                   cols.map(c => (
                                       <th>{c}</th>
                                   ))}
                               </tr>
                           </thead>
                           <tbody>
                                {
                                /* Go through each row, and output its value corresponding to the column */
                                rows.map((row,i)=> (
                                    <tr>
                                        {cols.map(c => (
                                            <td>{String(row[c])}</td>
                                        ))}
                                    </tr>
                                ))}
                           </tbody>
                       </table>
                    </div>
               )
            }
        </div>
    )
}