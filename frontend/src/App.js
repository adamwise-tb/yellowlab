import { useState } from "react";
import 'bootstrap/dist/css/bootstrap.min.css'; // Leveraging bootstrap to make the app look nice easilyimport { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";
import { BrowserRouter as Router, Routes, Route, Link } from "react-router-dom";

// Add for routing through links
import Home from "./pages/Home";
import Item from "./pages/Item";
import Distributor from "./pages/Distributor";
import EditItem from "./pages/EditItem";
import CreateDistributor from "./pages/CreateDistributor";

export default function App() {
    return (
      <Router>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/items/:id" element={<Item />} />
            <Route path="/items/:id/edit" element={<EditItem />} />
            <Route path="/distributors/:id" element={<Distributor />} />
            <Route path="/distributors/create" element={<CreateDistributor />} />
          </Routes>
      </Router>
    )
}