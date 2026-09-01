import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Hero from './sections/Hero';
import HowItWorks from './sections/HowItWorks';
import WhyNextcoder from './sections/WhyNextcoder';
import LearningPath from './sections/LearningPath';
import ProblemLibrary from './sections/ProblemLibrary';
import Workspace from './sections/Workspace';
import Stats from './sections/Stats';
import Community from './sections/Community';
import CTA from './sections/CTA';
import Footer from './components/Footer';
import Login from './pages/Login';
import Register from './pages/Register';
import ProblemsPage from './pages/problems/ProblemsPage';
import ProblemDetailPage from './pages/problems/ProblemDetailPage';
import ContestsSection from './components/Contests/ContestsSection';
import ContestDetail from './components/Contests/ContestDetail';
import AdminCreateExam from './pages/admin/AdminCreateExam';
import NotificationsPage from './pages/NotificationsPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import DiscussionPage from './pages/discussion/DiscussionPage';
import './styles/global.css';

function LandingPage() {
  return (
    <div className="app">
      <Navbar />
      <main>
        <Hero />
        <HowItWorks />
        <WhyNextcoder />
        <LearningPath />
        <ProblemLibrary />
        <Workspace />
        <Stats />
        <Community />
        <CTA />
      </main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/"                    element={<LandingPage />} />
        <Route path="/login"               element={<Login />} />
        <Route path="/register"            element={<Register />} />
        <Route path="/problems"            element={<ProblemsPage />} />
        <Route path="/problems/:id"        element={<ProblemDetailPage />} />
        <Route path="/contests"            element={<ContestsSection />} />
        <Route path="/contests/:examId"    element={<ContestDetail />} />
        <Route path="/discussion"          element={<DiscussionPage />} />
        <Route path="/notifications"        element={<NotificationsPage />} />
        <Route path="/dashboard"           element={<DashboardPage />} />
        <Route path="/admin/exams/create"  element={<AdminCreateExam />} />
      </Routes>
    </BrowserRouter>
  );
}
