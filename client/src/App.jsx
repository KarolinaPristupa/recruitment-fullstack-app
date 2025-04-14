import './App.module.scss';
import Header from "@components/header/index.jsx";
import Footer from "@components/footer/index.jsx";
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from "@pages/home";
import About from '@pages/about';
import Account from '@pages/account';
import AdminPage from "@pages/admin";
import CandidatePage from "@pages/candidate";
import EmployeePage from "@pages/employee";
import Vacancies from "@pages/vacancies";
// import Favorites from "@pages/favorites";
// import Users from "@pages/users";
import Notifications from "@pages/notifications";
import Candidates from "@pages/candidates";
import CreateVacancy from "@pages/createVacancy";
import VacancyEdit from "@pages/editVacancy";
import InviteCandidate from "@pages/inviteCandidate";
import Modal_invite from "@components/Modal_invite/index.jsx";
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

                        <Route path="/admin" element={<AdminPage />} />
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
                        <Route path="/candidates/invite/select" element={<Modal_invite />} />
                    </Routes>
                </main>
                <Footer />
            </div>
        </Router>
    );
}

export default App;
