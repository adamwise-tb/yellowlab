import { useState } from "react";
import 'bootstrap/dist/css/bootstrap.min.css'; // Leveraging bootstrap to make the app look nice easilyimport { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";

// Add for routing through links
import Home from "./pages/Home";
import Item from "./pages/Item";
import Distributor from "./pages/Distributor";

export default function App() {
    const [rows, setRows] = useState([]); // initialize state values to empty arrays
    const [loading, setLoading] = useState(false); // initialize state values to false
    const [active, setActive] = useState(null); // initialize button state

    // We'll use this function to ping the API we built with Java
    const apiRequest = async (path) => {
        setActive(path); // make the button blue
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

    return (
      <Router>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/items/:id" element={<Item />} />
            <Route path="/distributors/:id" element={<Distributor />} />
          </Routes>
      </Router>
    )
}