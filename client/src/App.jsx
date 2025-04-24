import './App.module.scss';
import Header from "@components/header/index.jsx";
import Footer from "@components/footer/index.jsx";
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from "@pages/home";
import About from '@pages/about';
import Account from '@pages/account';
import CandidatePage from "@pages/candidate";
import EmployeePage from "@pages/employee";
import Vacancies from "@pages/vacancies";
// import Favorites from "@pages/favorites";
// import Users from "@pages/users";
import Notifications from "@pages/notifications";
import Candidates from "@pages/candidates";
import CreateVacancy from "@pages/create-vacancy";
import VacancyEdit from "@pages/edit-vacancy";
import InviteCandidate from "@pages/invite-candidate";
import Report from "@pages/report";

function App() {
    return (
        <Router>
            <div className="App">
                <Header />
                <main>
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/about" element={<About />} />
                        <Route path="/account" element={<Account />} />

                        <Route path="/candidate" element={<CandidatePage />} />
                        <Route path="/employee" element={<EmployeePage />} />

                        <Route path="/vacancies/create" element={<CreateVacancy />} />
                        <Route path="/vacancies/edit" element={<VacancyEdit />} />
                        <Route path="/vacancies" element={<Vacancies />} />
                        {/*<Route path="/favorites" element={<Favorites />} />*/}
                        {/*<Route path="/users" element={<Users />} />*/}
                        <Route path="/notifications" element={<Notifications />} />
                        <Route path="/candidates" element={<Candidates />} />

                        <Route path="/candidates/invite" element={<InviteCandidate />} />
                        <Route path="/report/:interviewId" element={<Report />} />
                    </Routes>
                </main>
                <Footer />
            </div>
        </Router>
    );
}

export default App;